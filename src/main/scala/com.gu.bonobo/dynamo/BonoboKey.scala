package com.gu.bonobo.dynamo

import java.time.Instant
import com.gu.scanamo._

import com.gu.bonobo.model.{Key, KeyId, UserId}

case class BonoboKey(
  hashkey: String,
  rangekey: String,
  bonoboId: String,
  createdAt: Instant,
  extendedAt: Option[Instant],
  remindedAt: Option[Instant],
  keyValue: String,
  kongConsumerId: String,
  productName: String,
  productUrl: String,
  rpd: Int,
  rpm: Int,
  status: String,
  tier: String,
  labelIds: Vector[String]
) {
  def toKey = Key(
    hashkey,
    rangekey,
    KeyId(keyValue),
    UserId(kongConsumerId),
    createdAt,
    extendedAt,
    remindedAt
  )
}
