package com.gu.gibbons
package config

import cats.data.{Validated, ValidatedNel}
import cats.implicits._
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import java.time.Period
import scala.collection.JavaConverters._
import scala.util.Try
import model.Email



case class Settings(
  region: Regions,
  usersTableName: String,
  keysTableName: String,
  salt: String,
  bonoboUrl: String,
  fromAddress: Email,
  httpSettings: HttpSettings,
  dryRun: String
)

case class HttpSettings(
  connectionTimeout: Int,
  readTimeout: Int,
  idleConnections: Int,
  keepAlive: Int
)

object Settings {
  val inactivityPeriod = Period.ofMonths(30)
  val extensionGracePeriod = Period.ofWeeks(2)
  val verificationGracePeriod = Period.ofDays(1)
  val reminderSubject = "Your Content API keys are about to expire"
  val deletedSubject = "Your Content API keys have been deleted"

  def fromEnvironment: ValidatedNel[String, Settings] =
    parseEnv(System.getenv.asScala.toMap)

  def parseEnv(env: Map[String, String]) =
    (getEnv(env, "AWS_REGION").andThen(makeRegion),
     getEnv(env, "BONOBO_USERS_TABLE"),
     getEnv(env, "BONOBO_KEYS_TABLE"),
     getEnv(env, "SALT"),
     getEnv(env, "BONOBO_URL"),
     getEnv(env, "EMAIL_ORIGIN").map(Email(_)) ,
     getEnv(env, "DRY_RUN")).mapN(Settings(_, _, _, _, _, _, HttpSettings(2, 5, 5, 10), _))

  private def makeRegion(r: String): ValidatedNel[String, Regions] =
    Validated.fromTry {
      Try(Regions.fromName(r))
    }.bimap(_.getMessage, identity).toValidatedNel

  private def getEnv(env: Map[String, String], key: String): ValidatedNel[String, String] =
    env.get(key) match {
      case None    => (s"Missing $key").invalidNel
      case Some(x) => x.validNel
    }
}
