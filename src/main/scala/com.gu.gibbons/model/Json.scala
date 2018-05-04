package com.gu.gibbons
package model

import io.circe.{Encoder, Json}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import java.time.{Instant, OffsetDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

object JsonFormats {
    implicit val instant: Encoder[Instant] = new Encoder[Instant] {
        final def apply(t: Instant) = 
            Json.fromString(OffsetDateTime.ofInstant(t, ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
    }

    implicit val userId: Encoder[UserId] = deriveEncoder
    implicit val keyId: Encoder[KeyId] = deriveEncoder
    implicit val email: Encoder[Email] = deriveEncoder
    implicit val emailResult: Encoder[EmailResult] = deriveEncoder
    implicit val user: Encoder[User] = deriveEncoder
    implicit val key: Encoder[Key] = deriveEncoder
    
    implicit val encoder: Encoder[Result] = new Encoder[Result] {
        final def apply(res: Result) = res match {
            case DryRun(keys) =>
                val objs = keys.map { case (userId, keys) => Json.obj(
                    "user" -> Json.fromString(userId.id),
                    "keys" -> Json.arr(keys.map(_.asJson):_*)
                )}.toSeq
                Json.arr(objs:_*)
            case FullRun(result) => 
                val objs = result.map { case (userId, (emailRes, keys)) => Json.obj(
                    "user" -> Json.fromString(userId.id),
                    "email" -> emailRes.asJson,
                    "keys" -> Json.arr(keys.map(_.asJson):_*)
                )}.toSeq
                Json.arr(objs:_*)
        }
    }
}