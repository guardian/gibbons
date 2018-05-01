package com.gu.bonobo
package config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import java.time.Period
import java.net.URL

import model.Email

case class Settings(
  credentialsProvider: AWSCredentialsProvider,
  region: Regions,
  email: EmailSettings,
  users: DynamoSettings,
  keys: DynamoSettings,
  kongServerProtocol: String,
  kongServerName: String,
  /** The amount of inactivity time after which a key may be removed */
  inactivityPeriod: Period,
  /** The amount of gracing time we give users to let us know they still use their keys */
  gracePeriod: Period
)

case class EmailSettings(
  reminderSubject: String,
  deletedSubject: String,
  lambdaYesUrl: URL,
  lambdaNoUrl: URL,
  /** The email address used in the From field of emails sent to API users */
  origin: Email
)

case class DynamoSettings(
  tableName: String,
)