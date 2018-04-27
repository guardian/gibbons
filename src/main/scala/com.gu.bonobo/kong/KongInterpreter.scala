package com.gu.bonobo.kong

import cats.~>
import io.circe.parser._
import com.gu.bonobo.model.{KeyId, UserId}
import com.gu.bonobo.services.LoggingService
import monix.eval.Task
import okhttp3.{OkHttpClient, Request, Response}

class KongInterpreter(logger: LoggingService[Task]) extends (KongServiceF ~> Task) {
  import KongKey._
  import KongListConsumerKeys._

  private val client = new OkHttpClient();

  // TODO pass configuration down from above
  val serverUrl = ""

  def apply[A](op: KongServiceF[A]): Task[A] = op match {
    case DeleteKey(consumerId) => for {
      kongKey <- getKeyIdFor(consumerId)
      _ <- deleteKey(kongKey)
    } yield ()
    case GetKey(consumerId) => getKeyIdFor(consumerId)
  }

  private def getKeyIdFor(consumerId: UserId): Task[Option[KongKey]] = Task {
    val request = new Request.Builder().url(s"$serverUrl/consumers/$consumerId/key-auth").build
    val response = client.newCall(request).execute()
    
    response.code match {
      case 200 => 
        decode[KongListConsumerKeys](response.body.string) match {
          case Right(KongListConsumerKeys(key :: _)) => Some(key)
          case Right(_) => None
          case Left(err) => fail(s"JSON decoding error: $err")
        }
      case _ => queryFail(response)
    }
  }

  private def deleteKey(kongKey: KongKey): Task[Unit] = Task {
    val request = new Request.Builder().url(s"$serverUrl/consumers/$consumerId/key-auth/${kongKey.id}").delete()
    val response = client.newCall(request).execute()

    response.code match {
      case 204 => ()
      case _ => queryFail(response)
    }
  }

  private def queryFail(resp: Response) =
    fail(s"Something went wrong while querying Kong: ${response.code} - ${response.message}")

  private def fail(str: String) = {
    logger.warn(str)
    throw new Throwable(str)
  }
}