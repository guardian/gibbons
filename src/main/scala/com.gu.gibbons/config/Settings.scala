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
)

case class EmailSettings(
  lambdaYesUrl: String,
  lambdaNoUrl: String,
  nonce: String,
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

  def fromEnvironment: Option[Settings] = {
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
      Settings(
        Regions.fromName(region),
        EmailSettings(yesUrl, noUrl, nonce, Email(origin)),
        DynamoSettings(usersTableName), DynamoSettings(keysTableName),
        kongBasePath
      )
    }
  }
}