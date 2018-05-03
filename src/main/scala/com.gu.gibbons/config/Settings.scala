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

case class InteractionSettings(
  region: Regions,
  users: DynamoSettings,
  keys: DynamoSettings,
  kongServerBasePath: String,
  nonce: String,
) extends Settings

case class EmailSettings(
  lambdaYesUrl: String,
  lambdaNoUrl: String,
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

object ScheduledSettings {

  def fromEnvironment: Option[ScheduledSettings] = {
    val env = System.getenv.asScala
    for{
      region <- env.get("AWS_REGION")
      kongBasePath <- env.get("KONG_BASE_PATH")
      yesUrl <- env.get("GATEWAY_API_YES")
      noUrl  <- env.get("GATEWAY_API_NO")
      nonce <- env.get("GATEWAY_API_SECRET")
      origin <- env.get("EMAIL_ORIGIN")
      usersTableName <- env.get("BONOBO_USERS_TABLE")
      keysTableName <- env.get("BONOBO_KEYS_TABLE")
    } yield {
      ScheduledSettings(
        Regions.fromName(region),
        DynamoSettings(usersTableName), DynamoSettings(keysTableName),
        kongBasePath,
        nonce,
        EmailSettings(yesUrl, noUrl, Email(origin)),
      )
    }
  }
}

object InteractionSettings {
  def fromEnvironment: Option[InteractionSettings] = {
    val env = System.getenv.asScala
    for{
      region <- env.get("AWS_REGION")
      kongBasePath <- env.get("KONG_BASE_PATH")
      usersTableName <- env.get("BONOBO_USERS_TABLE")
      keysTableName <- env.get("BONOBO_KEYS_TABLE")
      nonce <- env.get("GATEWAY_API_SECRET")
    } yield {
      InteractionSettings(
        Regions.fromName(region),
        DynamoSettings(usersTableName), DynamoSettings(keysTableName),
        kongBasePath,
        nonce
      )
    }
  }
}