package com.gu.gibbons
package model

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.gu.scanamo.DynamoFormat
import com.gu.scanamo.error.{ DynamoReadError, MissingProperty }
import java.time.Instant
import scala.collection.JavaConverters._

/** A developer using the Content API
 *
 * @param name the person's name
 * @param email the person's email address
 */
case class User(
  id: UserId,
  name: String,
  email: Email,
  createdAt: Long,
  verificationSentAt: Option[Long],
  verified: Option[Boolean]
)

object User {
  implicit val dynamoFormat = new DynamoFormat[User] {
    def read(av: AttributeValue) =
      (for {
        attrs <- Option(av.getM).map(_.asScala)
        id <- attrs.get("id").flatMap(a => Option(a.getS))
        email <- attrs.get("email").flatMap(a => Option(a.getS))
        name <- attrs.get("name").flatMap(a => Option(a.getS))
        createdAt <- attrs.get("createdAt").flatMap(a => Option(a.getN).map(_.toLong))
      } yield
        User(
          UserId(id),
          name,
          Email(email, Some(name)),
          createdAt,
          attrs.get("verificationSentAt").flatMap(a => Option(a.getN)).map(_.toLong),
          attrs.get("verified").flatMap(a => Option(a.getN)).map(_.toBoolean),
        )).fold(Left(MissingProperty): Either[DynamoReadError, User])(Right(_))

    // we will never add a new record
    def write(u: User) = new AttributeValue()
  }

  def create(id: String,
             name: String,
             email: String,
             createdAt: String,
             verificationSentAt: Option[String] = None,
             verified: Option[String] = None
            ) =
    User(
      UserId(id),
      name,
      Email(email),
      Instant.parse(createdAt).toEpochMilli,
      verificationSentAt.map(Instant.parse(_)).map(_.toEpochMilli),
      verified.map(_.toBoolean)
    )
}

/** Spurious wrapper to read from Dynamo */
case class Key(
  userId: UserId,
  rangeKey: String,
  consumerId: String,
  tier: String,
  createdAt: Long,
  remindedAt: Option[Long],
  extendedAt: Option[Long])


object Key {
  implicit val format = new DynamoFormat[Key] {
    def read(av: AttributeValue) =
      (for {
        attrs <- Option(av.getM).map(_.asScala)
        userId <- attrs.get("bonoboId").flatMap(a => Option(a.getS))
        rangeKey <- attrs.get("rangekey").flatMap(a => Option(a.getS))
        consumerId <- attrs.get("kongConsumerId").flatMap(a => Option(a.getS))
        tier <- attrs.get("tier").flatMap(a => Option(a.getS))
        createdAt <- attrs.get("createdAt").flatMap(a => Option(a.getN).map(_.toLong))
      } yield Key(
        UserId(userId),
        rangeKey,
        consumerId,
        tier,
        createdAt,
        attrs.get("remindedAt").flatMap(a => Option(a.getN)).map(_.toLong),
        attrs.get("extendedAt").flatMap(a => Option(a.getN)).map(_.toLong)
      )).fold(Left(MissingProperty): Either[DynamoReadError, Key])(Right(_))

    // will never happen
    def write(k: Key) = new AttributeValue()
  }
  def create(id: String,
             rangeKey: String,
             consumerId: String,
             tier: String,
             createdAt: String,
             remindedAt: Option[String] = None,
             extendedAt: Option[String] = None) =
    Key(
      UserId(id),
      rangeKey,
      consumerId,
      tier,
      Instant.parse(createdAt).toEpochMilli,
      remindedAt.map(Instant.parse(_)).map(_.toEpochMilli),
      extendedAt.map(Instant.parse(_)).map(_.toEpochMilli)
    )
}
