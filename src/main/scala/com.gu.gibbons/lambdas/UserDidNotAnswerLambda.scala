package com.gu.gibbons
package lambdas

import io.circe.syntax._
import java.time.Instant
import monix.execution.Scheduler.Implicits.global

import config._
import model.JsonFormats
import services.interpreters._

class UserDidNotAnswerLambda extends GenericLambda {
  import JsonFormats._

  override def go(settings: Settings, dryRun: Boolean) = 
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