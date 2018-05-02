package com.gu.gibbons
package config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import java.time.Period
import scala.collection.JavaConverters._

import model.Email

case class Settings(
  region: Regions,
  email: EmailSettings,
  users: DynamoSettings,
  keys: DynamoSettings,
  kongServerBasePath: String,
  /** The amount of inactivity time after which a key may be removed */
  inactivityPeriod: Period,
  /** The amount of gracing time we give users to let us know they still use their keys */
  gracePeriod: Period
)

case class EmailSettings(
  reminderSubject: String,
  deletedSubject: String,
  lambdaYesUrl: String,
  lambdaNoUrl: String,
  nonce: String,
  /** The email address used in the From field of emails sent to API users */
  origin: Email
)

case class DynamoSettings(
  tableName: String,
)

object Settings {
  def fromEnvironment: Option[Settings] = {
    val env = System.getenv.asScala
    for{
      region <- env.get("AWS_REGION")
      kongBasePath <- env.get("KONG_BASE_PATH")
      inactivityPeriod <- env.get("BONOBO_INACTIVITY_PERIOD").map(_.toInt)
      gracePeriod <- env.get("BONOBO_GRACE_PERIOD").map(_.toInt)
      reminderSubject <- env.get("EMAIL_REMINDER_SUBJECT")
      deletedSubject <- env.get("EMAIL_DELETED_SUBJECT")
      yesUrl <- env.get("GATEWAY_API_YES")
      noUrl  <- env.get("GATEWAY_API_NO")
      nonce <- env.get("GATEWAY_API_SECRET")
      origin <- env.get("EMAIL_ORIGIN")
      usersTableName <- env.get("BONOBO_USERS_TABLE")
      keysTableName <- env.get("BONOBO_KEYS_TABLE")
    } yield {
      Settings(
        Regions.fromName(region),
        EmailSettings(reminderSubject, deletedSubject, yesUrl, noUrl, nonce, Email(origin)),
        DynamoSettings(usersTableName), DynamoSettings(keysTableName),
        kongBasePath,
        Period.ofMonths(inactivityPeriod),
        Period.ofDays(gracePeriod)
      )
    }
  }
}