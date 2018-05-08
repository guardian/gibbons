package com.gu.gibbons
package config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import java.time.Period
import scala.collection.JavaConverters._

import model.Email

sealed trait Settings {
  def region: Regions
  def users: DynamoSettings
  def keys: DynamoSettings
  def kongServerBasePath: String
  def nonce: String
}

case class ScheduledSettings(
  region: Regions,
  users: DynamoSettings,
  keys: DynamoSettings,
  kongServerBasePath: String,
  nonce: String,
  email: EmailSettings
) extends Settings

case class EmailSettings(
  bonoboUrl: String,
  /** The email address used in the From field of emails sent to API users */
  origin: Email
)

object EmailSettings {
  val reminderSubject = "Subject"
  val deletedSubject = "Subject"
}

case class DynamoSettings(
  tableName: String,
)

object Settings {
  val inactivityPeriod = Period.ofMonths(30)
  val gracePeriod = Period.ofWeeks(2)
}

trait EnvGetter {
  def getEnv(env: Map[String, String], key: String): Either[String, String] =
    env.get(key).fold(Left(s"Missing $key"): Either[String, String])(Right(_))
}

object ScheduledSettings extends EnvGetter {

  def fromEnvironment: Either[String, ScheduledSettings] = {
    val env = System.getenv.asScala.toMap
    for{
      region <- getEnv(env, "AWS_REGION")
      kongBasePath <- getEnv(env, "KONG_BASE_PATH")
      bonoboUrl <- getEnv(env, "BONOBO_BASE_PATH")
      nonce <- getEnv(env, "GATEWAY_API_SECRET")
      origin <- getEnv(env, "EMAIL_ORIGIN")
      usersTableName <- getEnv(env, "BONOBO_USERS_TABLE")
      keysTableName <- getEnv(env, "BONOBO_KEYS_TABLE")
    } yield {
      ScheduledSettings(
        Regions.fromName(region),
        DynamoSettings(usersTableName), DynamoSettings(keysTableName),
        kongBasePath,
        nonce,
        EmailSettings(bonoboUrl, Email(origin)),
      )
    }
  }


}

