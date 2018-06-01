package com.gu.gibbons
package lambdas

import cats.data.{ Validated, ValidatedNel }
import com.amazonaws.services.lambda.runtime.Context
import io.circe.Json
import io.circe.parser.decode
import java.io.{InputStream, OutputStream}
import java.time.OffsetDateTime
import monix.eval.Task
import monix.execution.Scheduler
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

import config._
import services.interpreters._

abstract class GenericLambda(
  load: (Settings, EmailInterpreter, BonoboInterpreter, LoggingInterpreter) => Script[Task]
)(implicit sched: Scheduler) extends ResourceProvider {
  import cats.implicits._
  import io.circe.syntax._
  import model.JsonFormats._

  def handleRequest(is: InputStream, os: OutputStream, context: Context) = {
    ( Settings.fromEnvironment
    , readArgs(is)
    ).mapN { case (settings: Settings, dryRun: Boolean) =>
      for {
        logger <- LoggingInterpreter(this.getClass.toString)
        now <- Task.eval { OffsetDateTime.now }
        _ <- resources(settings, logger).bracket(go(_, logger, settings, now, dryRun))(cleanup _)
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

  def go(resources: Resources, logger: LoggingInterpreter, settings: Settings, now: OffsetDateTime, dryRun: Boolean): Task[Json] = {
    val bonobo = new BonoboInterpreter(settings, logger, resources.dynamo, resources.http, resources.url)
    val email = new EmailInterpreter(settings, logger, resources.email, resources.url)
    val script = load(settings, email, bonobo, logger)
    script.run(now, dryRun).map(_.asJson)
  }
 
  private def readArgs(is: InputStream): ValidatedNel[String, Boolean] = Validated.fromEither {
    val input = Source.fromInputStream(is).mkString
    decode[Boolean](input).leftMap(_.toString)
  }.toValidatedNel
}