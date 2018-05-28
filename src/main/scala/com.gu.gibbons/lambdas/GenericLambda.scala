package com.gu.gibbons
package lambdas

import cats.data.{ Validated, ValidatedNel }
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsyncClientBuilder, AmazonDynamoDBAsync}
import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailServiceAsyncClientBuilder, AmazonSimpleEmailServiceAsync}
import io.circe.Json
import io.circe.parser.decode
import java.io.{InputStream, OutputStream}
import java.util.concurrent.TimeUnit
import monix.eval.Task
import monix.execution.Scheduler
import okhttp3.{OkHttpClient, ConnectionPool}
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.io.Source

import config._
import services.interpreters._

abstract class GenericLambda(implicit sched: Scheduler) {
  import cats.implicits._

  def handleRequest(is: InputStream, os: OutputStream, context: Context) = {
    ( Settings.fromEnvironment
    , readArgs(is)
    ).mapN { case (settings: Settings, dryRun: Boolean) =>
      for {
        logger <- LoggingInterpreter(this.getClass.toString)
        _ <- resources(settings, logger).bracket(go(_, logger, settings, dryRun))(cleanup _)
      } yield ()
    }.map { program =>
      Await.result(program.runAsync, Duration(context.getRemainingTimeInMillis, MILLISECONDS))
    }.fold(
      error => os.write(s"Something went horribly wrong: $error".getBytes), 
      result => os.write(result.toString.getBytes)
    )

    is.close()
    os.close()
  }

  def go(resources: Resources, logger: LoggingInterpreter, settings: Settings, dryRun: Boolean): Task[Json]
 
  private def readArgs(is: InputStream): ValidatedNel[String, Boolean] = Validated.fromEither {
    val input = Source.fromInputStream(is).mkString
    decode[Boolean](input).leftMap(_.toString)
  }.toValidatedNel

  private def resources(settings: Settings, logger: LoggingInterpreter): Task[Resources] = Task.evalOnce {
    val emailClient = AmazonSimpleEmailServiceAsyncClientBuilder.standard()
      .withRegion(settings.region)
      .build()

    val dynamoClient = AmazonDynamoDBAsyncClientBuilder.standard()
      .withRegion(settings.region)
      .build()

    val httpClient = new OkHttpClient.Builder()
      .connectTimeout(1, TimeUnit.SECONDS)
      .readTimeout(1, TimeUnit.SECONDS)
      .connectionPool(new ConnectionPool(5, 10, TimeUnit.SECONDS))
      .build

    Resources(emailClient, dynamoClient, httpClient)
  }.attempt.flatMap {
    case Left(error) => 
      logger.error(s"Failed to initialize resources: $error") >>= (_ => Task.raiseError(error))
    case Right(success) => Task.now(success)
  }

  private def cleanup(res: Resources): Task[Unit] = Task.eval {
    res.email.shutdown()
    res.dynamo.shutdown()
    res.http.dispatcher.executorService.shutdown()
  }

  protected case class Resources(email: AmazonSimpleEmailServiceAsync, dynamo: AmazonDynamoDBAsync, http: OkHttpClient)
}