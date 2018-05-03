package com.gu.gibbons
package lambdas

import com.amazonaws.services.lambda.runtime.Context; 
import java.io.{ InputStream, OutputStream }
import monix.execution.Scheduler.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

import config.InteractionSettings
import dynamo.BonoboInterpreter
import log4j.LoggingInterpreter
import kong.KongInterpreter
import model.KeyId

class UserSaidNoLambda extends RestApi {
  import cats.instances.option._
  import cats.syntax.flatMap._

  def handleRequest(is: InputStream, os: OutputStream, context: Context) = {
    val result = for {
      settings <- InteractionSettings.fromEnvironment
      keyId <- decodeParams(is, settings.nonce)
    } yield {
      val logger = new LoggingInterpreter()
      val kong = new KongInterpreter(settings, logger)
      val bonobo = new BonoboInterpreter(settings, kong, logger)

      val userSaidNo = new UserSaidNo(settings, bonobo, logger)

      Await.result(userSaidNo.run(keyId).runAsync, Duration(context.getRemainingTimeInMillis, MILLISECONDS))

      keyId
    }

    result match {
      case Left(msg) => os.write(s"Oops, something got wrong: $msg".getBytes)
      case Right(keyId) => os.write(s"Key $keyId has been deleted".getBytes)
    }

    is.close()
    os.close()
  }
}