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
  salt: String,
  bonoboUrl: String,
  fromAddress: Email,
  httpSettings: HttpSettings
)

case class HttpSettings(
  connectionTimeout: Int,
  readTimeout: Int,
  idleConnections: Int,
  keepAlive: Int
)

object Settings {
  val inactivityPeriod = Period.ofMonths(30)
  val gracePeriod = Period.ofWeeks(2)
  val reminderSubject = "Your Content API keys are about to expire"
  val deletedSubject = "Your Content API keys have been deleted"

  /** These accounts belong to the Guardian digital department and can
    *  safely be ignored
    */
  val whitelist = Set(
    "e3e345ef-b366-4641-c634-1228b1b9ff9a",
    "7d8f16ba-b57b-491e-91cf-d9a9d1431ccf",
    "10f1ba90-838e-4bc2-aa56-c084011012e6",
    "ea39a2bb-630d-4565-97ef-a47eff4ec300"
  )

  def fromEnvironment: ValidatedNel[String, Settings] =
    parseEnv(System.getenv.asScala.toMap)
    
  def parseEnv(env: Map[String, String]) =
    ( getEnv(env, "AWS_REGION").andThen(makeRegion)
    , getEnv(env, "BONOBO_USERS_TABLE")
    , getEnv(env, "SALT")
    , getEnv(env, "BONOBO_URL")
    , getEnv(env, "EMAIL_ORIGIN").map(Email(_))
    ).mapN(Settings(_, _, _, _, _, HttpSettings(1, 1, 5, 10)))

  private def makeRegion(r: String): ValidatedNel[String, Regions] = Validated.fromTry {
    Try(Regions.fromName(r))
  }.bimap(_.getMessage, identity).toValidatedNel

  private def getEnv(env: Map[String, String], key: String): ValidatedNel[String, String] =
    env.get(key) match {
      case None => (s"Missing $key").invalidNel
      case Some(x) => x.validNel
    }
}

