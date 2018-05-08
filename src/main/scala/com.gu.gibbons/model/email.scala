package com.gu.gibbons
package model

import io.circe.Encoder
import io.circe.generic.semiauto._

/** An email address */
case class Email(val email: String, val name: Option[String] = None)

/** The ID of the email having been sent */
case class EmailResult(val msgId: String) extends AnyVal

object EmailResult {
  implicit val encoder: Encoder[EmailResult] = deriveEncoder
}
