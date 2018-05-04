package com.gu.gibbons
package lambdas

import com.amazonaws.services.lambda.runtime.Context; 
import java.time.Instant
import io.circe.Json
import io.circe.syntax._
import java.io.{InputStream, OutputStream}
import monix.execution.Scheduler.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

import config._
import model.JsonFormats
import dynamo.BonoboInterpreter
import log4j.LoggingInterpreter
import kong.KongInterpreter
import ses.EmailInterpreter

class ScheduledLambda {
  import cats.instances.either._
  import cats.syntax.flatMap._
  import JsonFormats._

  def handleRequest(is: InputStream, os: OutputStream, context: Context) = {
    val result = ScheduledSettings.fromEnvironment >>= { settings =>

      val program = for {
        logger <- LoggingInterpreter.apply
        _ <- logger.info("Hello")
        _ <- logger.info("Opening up a connection to Kong...")
        kong <- KongInterpreter(settings, logger)
        _ <- logger.info("Opening up a connection to Bonobo...")
        bonobo <- BonoboInterpreter(settings, kong, logger)
        _ <- logger.info("Opening up a connection to SES...")
        email <- EmailInterpreter(settings, logger)
        _ <- logger.info("We're all set, starting...")
        // userDidNotAnswer = new UserDidNotAnswer(settings, email, bonobo, logger)
        userReminder = new UserReminder(settings, email, bonobo, logger)
        // rDel <- userDidNotAnswer.run(true)
        rRem <- userReminder.run(Instant.now, false)
        _ <- logger.info("Goodbye")
      } yield Json.obj(
          "deletions" -> Json.Null, 
          "reminders" -> rRem.asJson
      )

      val result = Await.result(program.runAsync, Duration(context.getRemainingTimeInMillis, MILLISECONDS))

      Right(result)
    }

    result match {
      case Left(error) =>
        os.write(s"Something went horribly wrong: $error".getBytes)
      case Right(result) =>
        os.write(result.toString.getBytes)
    }
    is.close()
    os.close()
  }
}