package com.gu.gibbons
package model

import io.circe.{ Encoder, Json, JsonObject, KeyEncoder }
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import java.time.{ Instant, OffsetDateTime, ZoneOffset }
import java.time.format.DateTimeFormatter

object JsonFormats {
  implicit val instant: Encoder[Instant] = new Encoder[Instant] {
    final def apply(t: Instant) =
      Json.fromString(OffsetDateTime.ofInstant(t, ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
  }

  implicit val email: Encoder[Email] = deriveEncoder
  implicit val emailResult: Encoder[EmailResult] = deriveEncoder
  implicit val user: Encoder[User] = deriveEncoder

  implicit val userId: Encoder[UserId] = new Encoder[UserId] {
    final def apply(uid: UserId) = Json.fromString(UserId.unwrap(uid))
  }

  implicit val userIdAsKey: KeyEncoder[UserId] = new KeyEncoder[UserId] {
    final def apply(uid: UserId) = UserId.unwrap(uid)
  }
}
