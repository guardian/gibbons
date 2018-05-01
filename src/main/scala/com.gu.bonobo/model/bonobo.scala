package com.gu.bonobo
package model

// import db._
import java.time.Instant

/** A developer using the Content API
  * 
  * @param name the person's name
  * @param email the person's email address
  * @param company the person's company  name
  * @param url the person's company website url
  */
case class User(
    id: UserId,
    name: String,
    email: Email
)

object User {
  def create(id: String, name: String, email: String) =
    User(UserId(id), name, Email(email))
}

/** An identifier for API users */
case class UserId(val id: String) extends AnyVal

/** An identifier for API keys */
case class KeyId(val id: String) extends AnyVal

/** A Content API key with associated limits
  * 
  * @param id the key id
  * @param createdOn the time at which that key was created
  * @param extendedOn the time at which that key was extended for another 30-months period
  * @param remindedOn the time at which a reminder email was sent
  */
case class Key(
  hashKey: String,
  rangeKey: String,
  id: KeyId,
  userId: UserId,
  createdOn: Instant,
  extendedOn: Option[Instant],
  remindedOn: Option[Instant]
)

object Key {
  def create(id: String, userId: String, createdOn: String, extendedOn: Option[String] = None, remindedOn: Option[String] = None) =
    Key("", "", KeyId(id), UserId(userId), Instant.parse(createdOn), extendedOn.map(Instant.parse(_)), remindedOn.map(Instant.parse(_)))
}


sealed trait BonoboResult
case object Success extends BonoboResult
case class KeyNotFound(keyId: KeyId) extends BonoboResult
