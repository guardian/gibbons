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
    val bonobo = new BonoboInterpreter(settings, logger, resources.dynamo, resources.http)
    val email = new EmailInterpreter(settings, logger, resources.email)
    
    for {
      _ <- logger.info("Hello")
      userDidNotAnswer = new UserDidNotAnswer(settings, email, bonobo, logger)
      rDel <- userDidNotAnswer.run(dryRun)
      _ <- logger.info("Goodbye")
    } yield rDel.asJson
  }
}