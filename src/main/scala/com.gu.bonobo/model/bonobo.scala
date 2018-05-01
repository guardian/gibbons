package com.gu.bonobo
package model

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.gu.scanamo.DynamoFormat
import com.gu.scanamo.error.{DynamoReadError, MissingProperty}
import java.time.Instant
import scala.collection.JavaConverters._

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
  implicit val dynamoFormat = new DynamoFormat[User] {
    def read(av: AttributeValue) = (for {
      attrs <- Option(av.getM).map(_.asScala)
      id <- attrs.get("id").flatMap(a => Option(a.getS))
      email <- attrs.get("email").flatMap(a => Option(a.getS))
      name <- attrs.get("name").flatMap(a => Option(a.getS))
    } yield User(UserId(id), name, Email(email))).fold(Left(MissingProperty): Either[DynamoReadError, User])(Right(_))

    // we will never add a new record
    def write(u: User) = new AttributeValue()
  }

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
  userId: UserId,
  kongId: KeyId,
  keyValue: String,
  createdOn: Instant,
  extendedOn: Option[Instant],
  remindedOn: Option[Instant]
)

object Key {
  implicit val dynamoFormat = new DynamoFormat[Key] {
    def read(av: AttributeValue) = (for {
      attrs <- Option(av.getM).map(_.asScala)
      hashKey <- attrs.get("hashkey").flatMap(a => Option(a.getS))
      rangeKey <- attrs.get("rangekey").flatMap(a => Option(a.getS))
      userId <- attrs.get("bonoboId").flatMap(a => Option(a.getS))
      createdAt <- attrs.get("createdAt").flatMap(a => Option(a.getN)).map(_.toLong)
      extendedAt <- attrs.get("extendedAt").flatMap(a => Option(a.getN)).map(n => Some(n.toLong)).orElse(Some(None))
      remindedAt <- attrs.get("remindedAt").flatMap(a => Option(a.getN)).map(n => Some(n.toLong)).orElse(Some(None))
      keyValue <- attrs.get("keyValue").flatMap(a => Option(a.getS))
      kongId <- attrs.get("kongConsumerId").flatMap(a => Option(a.getS))
    } yield Key(
      hashKey, rangeKey, UserId(userId), KeyId(kongId), keyValue, 
      Instant.ofEpochMilli(createdAt),
      extendedAt.map(Instant.ofEpochMilli(_)),
      remindedAt.map(Instant.ofEpochMilli(_))
    )).fold(Left(MissingProperty): Either[DynamoReadError, Key])(Right(_))

    // we will never add a new record
    def write(k: Key) = new AttributeValue()
  }

  def create(id: String, userId: String, createdOn: String, extendedOn: Option[String] = None, remindedOn: Option[String] = None) =
    Key("", "", UserId(userId), KeyId(id), "", Instant.parse(createdOn), extendedOn.map(Instant.parse(_)), remindedOn.map(Instant.parse(_)))
}


sealed trait BonoboResult
case object Success extends BonoboResult
case class KeyNotFound(keyId: KeyId) extends BonoboResult
