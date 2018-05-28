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

  override def go(resources: Resources, logger: LoggingInterpreter, settings: Settings, dryRun: Boolean) = {
    val bonobo = new BonoboInterpreter(settings, logger, resources.dynamo, resources.http, resources.url)
    val email = new EmailInterpreter(settings, logger, resources.email, resources.url)
    val userReminder = new UserReminder(settings, email, bonobo, logger)

    userReminder.run(Instant.now, dryRun).map(_.asJson)
  }
}