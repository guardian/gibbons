package com.gu.gibbons
package lambdas

import com.amazonaws.services.lambda.runtime.Context; 
import java.time.Instant
import monix.execution.Scheduler.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

import config._
import dynamo.BonoboInterpreter
import log4j.LoggingInterpreter
import kong.KongInterpreter
import ses.EmailInterpreter

class ScheduledLambda {
  import cats.instances.option._
  import cats.syntax.flatMap._

  def handleRequest(x: Any, context: Context) = {
    ScheduledSettings.fromEnvironment >>= { settings =>
      val logger = new LoggingInterpreter()
      val kong = new KongInterpreter(settings, logger)
      val bonobo = new BonoboInterpreter(settings, kong, logger)
      val email = new EmailInterpreter(settings, logger)

      val userDidNotAnswer = new UserDidNotAnswer(settings, email, bonobo, logger)
      val userReminder = new UserReminder(settings, email, bonobo, logger)
      val program = for {
        _ <- userDidNotAnswer.run
        _ <- userReminder.run(Instant.now)
      } yield ()

      Await.result(program.runAsync, Duration(context.getRemainingTimeInMillis, MILLISECONDS))

      None
    }
  }
}