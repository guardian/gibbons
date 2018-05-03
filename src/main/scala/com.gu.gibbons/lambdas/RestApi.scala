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

  protected def decodeParams(is: InputStream, nonce: String): Either[String, KeyId] = {
    val input = Source.fromInputStream(is).mkString
    decode[Params](input).toOption match {
      case Some(params) => 
        val hash = hasher.hash(params.keyId, nonce)
        if (hash == params.hash)
          Right(KeyId(params.keyId))
        else
          Left("Key and hash don't match")
      case None =>
        Left("Missing query parameters")
    }
  }

  private case class Params(keyId: String, hash: String)

  private object Params {
    implicit val decoder: Decoder[Params] = deriveDecoder
  }

}