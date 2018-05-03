package com.gu.gibbons.lambdas

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser.decode
import java.io.{ InputStream, OutputStream }
import scala.io.Source

import com.gu.gibbons.model.KeyId
import com.gu.gibbons.ses.HashGenerator

abstract class RestApi {

  val hasher = new HashGenerator()

  protected def decodeParams(is: InputStream, nonce: String): Option[KeyId] = {
    val input = Source.fromInputStream(is).mkString
    decode[Params](input).toOption.flatMap { params =>
      val hash = hasher.hash(params.keyId, nonce)
      if (hash == params.hash)
        Some(KeyId(params.keyId))
      else
        None
    }
  }

  private case class Params(keyId: String, hash: String)

  private object Params {
    implicit val decoder: Decoder[Params] = deriveDecoder
  }

}