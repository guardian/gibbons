package com.gu.gibbons.kong

import io.circe._
import io.circe.generic.semiauto._

case class KongListConsumerKeys(data: List[KongKey])

object KongListConsumerKeys {
  implicit val decoder: Decoder[KongListConsumerKeys] = deriveDecoder
}

case class KongKey(id: String)

object KongKey {
  implicit val decoder: Decoder[KongKey] = deriveDecoder[KongKey]
}