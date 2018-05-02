package com.gu.gibbons.kong

import cats.free.Free
import com.gu.gibbons.model.{UserId, Key}

sealed trait KongServiceF[A]
case class DeleteKey(consumerId: UserId) extends KongServiceF[Unit]
case class GetKey(consumerId: UserId) extends KongServiceF[Option[KongKey]]

object KongService {
  type KongService[A] = Free[KongServiceF, A]
  def deleteKey(consumerId: UserId): KongService[Unit] = Free.liftF(DeleteKey(consumerId))
  def getKey(consumerId: UserId): KongService[Option[KongKey]] = Free.liftF(GetKey(consumerId))
}