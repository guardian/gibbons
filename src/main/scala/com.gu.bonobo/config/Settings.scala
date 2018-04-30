package com.gu.bonobo
package config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions

case class Settings(
  credentialsProvider: AWSCredentialsProvider,
  region: Regions,
  email: EmailSettings,
  users: DynamoSettings,
  keys: DynamoSettings
)

case class EmailSettings(
  reminderSubject: String,
  deletedSubject: String
)

case class DynamoSettings(
  tableName: String
)