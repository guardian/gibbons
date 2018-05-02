package com.gu.gibbons
package lambdas

import com.amazonaws.services.lambda.runtime.Context; 
import java.io.{ InputStream, OutputStream }
import monix.execution.Scheduler.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

import config.Settings
import dynamo.BonoboInterpreter
import log4j.LoggingInterpreter
import kong.KongInterpreter
import model.KeyId

class UserSaidYesLambda extends RestApi {
  import cats.instances.option._
  import cats.syntax.flatMap._

  def handleRequest(is: InputStream, os: OutputStream, context: Context) = {
    for {
      settings <- Settings.fromEnvironment
      keyId <- decodeParams(is, settings.email.nonce)
    } yield {
      val logger = new LoggingInterpreter()
      val kong = new KongInterpreter(settings, logger)
      val bonobo = new BonoboInterpreter(settings, kong, logger)

      val userSaidYes = new UserSaidYes(settings, bonobo, logger)

      Await.result(userSaidYes.run(keyId).runAsync, Duration(context.getRemainingTimeInMillis, MILLISECONDS))

      None
    }
  }
}