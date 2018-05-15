package com.gu.gibbons
package lambdas

import cats.syntax.either._
import com.amazonaws.services.lambda.runtime.Context; 
import java.time.Instant
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.Json
import java.io.{InputStream, OutputStream}
import monix.execution.Scheduler.Implicits.global
import monix.eval.Task
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.Source

import config._
import model.{JsonFormats, Result}
import services.interpreters._

class UserReminderLambda {
  import cats.instances.either._
  import cats.syntax.flatMap._
  import JsonFormats._

  def handleRequest(is: InputStream, os: OutputStream, context: Context) = {
    go(is).map { program =>
      Await.result(program.runAsync, Duration(context.getRemainingTimeInMillis, MILLISECONDS))
    } match {
      case Right(result) => os.write(result.toString.getBytes)
      case Left(error) => os.write(s"Something went horribly wrong: $error".getBytes)
    }

    is.close()
    os.close()
  }

  def go(is: InputStream): Either[String, Task[Json]] = for {
    settings <- Settings.fromEnvironment
    dryRun <- readArgs(is)
  } yield {
    for {
      logger <- LoggingInterpreter.apply
      _ <- logger.info("Hello")
      _ <- logger.info("Opening up a connection to Bonobo...")
      bonobo <- BonoboInterpreter(settings, logger)
      _ <- logger.info("Opening up a connection to SES...")
      email <- EmailInterpreter(settings, logger)
      _ <- logger.info("We're all set, starting...")
      userReminder = new UserReminder(settings, email, bonobo, logger)
      rRem <- userReminder.run(Instant.now, dryRun)
      _ <- logger.info("Goodbye")
    } yield rRem.asJson
  }

  def readArgs(is: InputStream): Either[String, Boolean] = {
    val input = Source.fromInputStream(is).mkString
    decode[Boolean](input).leftMap(_.toString)
  }
}