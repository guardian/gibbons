package com.gu.gibbons.kong

import cats.~>
import io.circe.parser._
import monix.eval.Task
import okhttp3.{OkHttpClient, Request, Response}

import com.gu.gibbons.config.Settings
import com.gu.gibbons.model.{KeyId, UserId}
import com.gu.gibbons.services.LoggingService

class KongInterpreter(settings: Settings, logger: LoggingService[Task]) extends (KongServiceF ~> Task) {
  import KongKey._
  import KongListConsumerKeys._

  private val client = new OkHttpClient();

  def apply[A](op: KongServiceF[A]): Task[A] = op match {
    case DeleteKey(consumerId) => for {
      kongKey <- getKeyIdFor(consumerId)
      _ <- kongKey.fold(Task.now(()))(deleteKey(consumerId, _))
    } yield ()
    case GetKey(consumerId) => getKeyIdFor(consumerId)
  }

  private def getKeyIdFor(consumerId: UserId): Task[Option[KongKey]] = Task {
    val request = new Request.Builder().url(keyAuthUrl(consumerId)).build
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

  private def deleteKey(consumerId: UserId, kongKey: KongKey): Task[Unit] = Task {
    val request = new Request.Builder().url(keyAuthUrl(consumerId, Some(kongKey.id))).delete().build
    val response = client.newCall(request).execute()

    response.code match {
      case 204 => ()
      case _ => queryFail(response)
    }
  }

  private def queryFail(resp: Response) =
    fail(s"Something went wrong while querying Kong: ${resp.code} - ${resp.message}")

  private def fail(str: String) = {
    logger.warn(str)
    throw new Throwable(str)
  }

  private def keyAuthUrl(consumerId: UserId, keyId: Option[String] = None) =
    s"""${settings.kongServerBasePath}/consumers/${consumerId.id}/key-auth${keyId.map("/" + _).getOrElse("")}"""
}