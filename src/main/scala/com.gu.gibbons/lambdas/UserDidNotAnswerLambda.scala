package com.gu.gibbons
package lambdas

import cats.data.{ Validated, ValidatedNel }
import com.amazonaws.services.lambda.runtime.Context; 
import java.time.Instant
import io.circe.Json
import io.circe.parser.decode
import io.circe.syntax._
import java.io.{InputStream, OutputStream}
import monix.execution.Scheduler.Implicits.global
import monix.eval.Task
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

import config._
import model.JsonFormats
import services.interpreters._

class UserDidNotAnswerLambda {
  import cats.implicits._
  import JsonFormats._

  def handleRequest(is: InputStream, os: OutputStream, context: Context) = {
    go(is).map { program =>
      Await.result(program.runAsync, Duration(context.getRemainingTimeInMillis, MILLISECONDS))
    }.fold(error => os.write(s"Something went horribly wrong: $error".getBytes), result => os.write(result.toString.getBytes))

    is.close()
    os.close()
  }

  def go(is: InputStream): ValidatedNel[String, Task[Json]] = 
    ( Settings.fromEnvironment
    , readArgs(is)
    ).mapN { case (settings, dryRun) => 
      for {
        logger <- LoggingInterpreter.apply
        _ <- logger.info("Hello")
        _ <- logger.info("Opening up a connection to Bonobo...")
        bonobo <- BonoboInterpreter(settings, logger)
        _ <- logger.info("Opening up a connection to SES...")
        email <- EmailInterpreter(settings, logger)
        _ <- logger.info("We're all set, starting...")
        userDidNotAnswer = new UserDidNotAnswer(settings, email, bonobo, logger)
        rDel <- userDidNotAnswer.run(dryRun)
        _ <- logger.info("Goodbye")
      } yield rDel.asJson
    }
 
  def readArgs(is: InputStream): ValidatedNel[String, Boolean] = Validated.fromEither {
    val input = Source.fromInputStream(is).mkString
    decode[Boolean](input).leftMap(_.toString)
  }.toValidatedNel
}