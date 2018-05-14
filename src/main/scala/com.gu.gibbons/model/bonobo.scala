package com.gu.gibbons
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
    email: Email,
    createdAt: Instant,
    remindedAt: Option[Instant],
    extendedAt: Option[Instant]
)

object User {
  implicit val dynamoFormat = new DynamoFormat[User] {
    def read(av: AttributeValue) = (for {
      attrs <- Option(av.getM).map(_.asScala)
      id <- attrs.get("id").flatMap(a => Option(a.getS))
      email <- attrs.get("email").flatMap(a => Option(a.getS))
      name <- attrs.get("name").flatMap(a => Option(a.getS))
      createdAt <- attrs.get("createdAt").flatMap(a => Option(a.getN).map(_.toLong))
      remindedAt <- attrs.get("remindedAt").map(a => Option(a.getS).map(_.toLong).orElse(None))
      extendedAt <- attrs.get("extendedAt").map(a => Option(a.getS).map(_.toLong).orElse(None))
    } yield User(
      UserId(id), 
      name, 
      Email(email, Some(name)), 
      Instant.ofEpochMilli(createdAt), 
      remindedAt.map(Instant.ofEpochMilli(_)), 
      extendedAt.map(Instant.ofEpochMilli(_))
    )).fold(Left(MissingProperty): Either[DynamoReadError, User])(Right(_))

    // we will never add a new record
    def write(u: User) = new AttributeValue()
  }

  def create(id: String, name: String, email: String) =
    User(UserId(id), name, Email(email), Instant.now, None, None)
}

/** An identifier for API users */
case class UserId(val id: String) extends AnyVal
