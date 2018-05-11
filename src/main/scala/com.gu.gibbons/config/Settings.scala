package com.gu.gibbons
package config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import java.time.Period
import scala.collection.JavaConverters._

import model.Email

case class Settings(
  region: Regions,
  users: DynamoSettings,
  keys: DynamoSettings,
  salt: String,
  bonoboUrl: String,
  email: EmailSettings
)

case class EmailSettings(
  /** The email address used in the From field of emails sent to API users */
  origin: Email
)

case class DynamoSettings(
  tableName: String,
)

object Settings {
  val inactivityPeriod = Period.ofMonths(30)
  val gracePeriod = Period.ofWeeks(2)
  val reminderSubject = "Your Content API keys are about to expire"
  val deletedSubject = "Your Content API keys have been deleted"

  def fromEnvironment: Either[String, Settings] = {
    val env = System.getenv.asScala.toMap
    for{
      region <- getEnv(env, "AWS_REGION")
      salt <- getEnv(env, "SALT")
      origin <- getEnv(env, "EMAIL_ORIGIN")
      bonoboUrl <- getEnv(env, "BONOBO_URL")
      usersTableName <- getEnv(env, "BONOBO_USERS_TABLE")
      keysTableName <- getEnv(env, "BONOBO_KEYS_TABLE")
    } yield {
      Settings(
        region = Regions.fromName(region),
        users = DynamoSettings(usersTableName), 
        keys = DynamoSettings(keysTableName),
        salt = salt,
        bonoboUrl = bonoboUrl,
        email = EmailSettings(Email(origin)),
      )
    }
  }

  private def getEnv(env: Map[String, String], key: String): Either[String, String] =
    env.get(key).fold(Left(s"Missing $key"): Either[String, String])(Right(_))
}

