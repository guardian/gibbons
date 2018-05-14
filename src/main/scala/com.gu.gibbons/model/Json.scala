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
    implicit val email: Encoder[Email] = deriveEncoder
    implicit val emailResult: Encoder[EmailResult] = deriveEncoder
    implicit val user: Encoder[User] = deriveEncoder
    
    implicit val encoder: Encoder[Result] = new Encoder[Result] {
        final def apply(res: Result) = res match {
            case DryRun(users) => users.asJson
            case FullRun(result) => 
                val objs = result.map { case (userId, emailRes) => Json.obj(
                    "user" -> Json.fromString(userId.id),
                    "email" -> emailRes.asJson
                )}.toSeq
                Json.arr(objs:_*)
        }
    }
}