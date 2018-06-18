package com.gu.gibbons
package model

import io.circe.{Encoder, Json, JsonObject}
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

    implicit val map: Encoder[Map[UserId, Option[EmailResult]]] = new Encoder[Map[UserId, Option[EmailResult]]] {
        final def apply(m: Map[UserId, Option[EmailResult]]) =
            Json.fromJsonObject(JsonObject(m.map { case (UserId(id), result) => id -> result.asJson }.toSeq: _*))
    }
}