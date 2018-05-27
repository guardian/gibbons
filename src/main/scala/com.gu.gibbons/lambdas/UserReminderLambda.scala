package com.gu.gibbons
package lambdas

import io.circe.syntax._
import java.time.Instant
import monix.execution.Scheduler.Implicits.global

import config._
import model.{JsonFormats, Result}
import services.interpreters._

class UserReminderLambda extends GenericLambda {
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
      userReminder = new UserReminder(settings, email, bonobo, logger)
      rRem <- userReminder.run(Instant.now, dryRun)
      _ <- logger.info("Goodbye")
    } yield rRem.asJson
}