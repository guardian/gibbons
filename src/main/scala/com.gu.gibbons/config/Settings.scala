package com.gu.gibbons
package config

import cats.data.ValidatedNel
import cats.implicits._
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import java.time.Period
import scala.collection.JavaConverters._

import model.Email

case class Settings(
  region: Regions,
  usersTableName: String,
  salt: String,
  bonoboUrl: String,
  fromAddress: Email
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

  def fromEnvironment: ValidatedNel[String, Settings] = {
    val env = System.getenv.asScala.toMap
    ( getEnv(env, "AWS_REGION").map(Regions.fromName(_))
    , getEnv(env, "BONOBO_USERS_TABLE")
    , getEnv(env, "SALT")
    , getEnv(env, "BONOBO_URL")
    , getEnv(env, "EMAIL_ORIGIN").map(Email(_))
    ).mapN(Settings(_, _, _, _, _))
  }

  private def getEnv(env: Map[String, String], key: String): ValidatedNel[String, String] =
    env.get(key) match {
      case None => (s"Missing $key").invalidNel
      case Some(x) => x.validNel
    }
}

