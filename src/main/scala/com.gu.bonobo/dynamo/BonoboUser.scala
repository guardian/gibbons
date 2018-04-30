package com.gu.bonobo.dynamo

import java.time.Instant
import com.gu.scanamo._

import com.gu.bonobo.model.{User, UserId, Email}

case class BonoboUser(
  id: String,
  createdAt: Instant,
  email: String,
  name: String,
  registrationType: String,
  labelIds: Option[Vector[String]],
  companyName: Option[String],
  companyUrl: Option[String],
  articlesPerDay: Option[Int],
  businessArea: Option[String],
  commercialModel: Option[String],
  content: Option[String],
  contentFormat: Option[String],
  monthlyUsers: Option[Int]
) {
  def toUser = User(
    UserId(id),
    name,
    Email(email)
  )
}