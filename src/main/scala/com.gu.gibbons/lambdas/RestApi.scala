package com.gu.gibbons.lambdas

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser.decode
import java.io.{ InputStream, OutputStream }
import java.security.MessageDigest
import scala.io.Source

import com.gu.gibbons.model.KeyId

abstract class RestApi {

  protected def decodeParams(is: InputStream, nonce: String): Option[KeyId] = {
    val input = Source.fromInputStream(is).mkString
    decode[Params](input).toOption.flatMap { params =>
      val hash = params.keyId + nonce
      if (md5.digest(hash.getBytes).toString == params.hash)
        Some(KeyId(params.keyId))
      else
        None
    }
  }

  private val md5 = MessageDigest.getInstance("MD5")

  private case class Params(keyId: String, hash: String)

  private object Params {
    implicit val decoder: Decoder[Params] = deriveDecoder
  }

}