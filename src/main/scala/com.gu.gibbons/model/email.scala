package com.gu.gibbons
package model

import io.circe.Encoder
import io.circe.generic.semiauto._

/** An email address */
case class Email(val email: String) extends AnyVal

/** The ID of the email having been sent */
case class EmailResult(val msgId: String) extends AnyVal

object EmailResult {
  implicit val encoder: Encoder[EmailResult] = deriveEncoder
}

/** An email destination
  * 
  * @param to A list of destination email addresses
  * @param cc A list of carbon-copy email addressses
  * @param bcc A list of blind carbon-copy email addresses
  */
case class Destination(
    to: Email,
    cc: Seq[Email] = Nil,
    bcc: Seq[Email] = Nil
)
