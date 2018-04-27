package com.gu.bonobo
package model

/** An email address */
case class Email(val email: String) extends AnyVal

/** The ID of the email having been sent */
case class EmailResult(val msgId: String) extends AnyVal

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

/** The content of an email
  *
  * @param subject The subject of the email
  * @param body The body of the email
  */
case class Message(
    subject: String,
    body: String
)