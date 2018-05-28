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

  override def go(resources: Resources, logger: LoggingInterpreter, settings: Settings, dryRun: Boolean) = {
    val bonobo = new BonoboInterpreter(settings, logger, resources.dynamo, resources.http, resources.url)
    val email = new EmailInterpreter(settings, logger, resources.email, resources.url)
    val userDidNotAnswer = new UserDidNotAnswer(settings, email, bonobo, logger)
    
    userDidNotAnswer.run(dryRun).map(_.asJson)
  }
}