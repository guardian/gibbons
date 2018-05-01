package com.gu.bonobo.kong

import cats.free.Free
import com.gu.bonobo.model.{UserId, Key}

sealed trait KongServiceF[A]
case class DeleteKey(consumerId: UserId) extends KongServiceF[Unit]
case class GetKey(consumerId: UserId) extends KongServiceF[Option[Key]]

object KongService {
  type KongService[A] = Free[KongServiceF, A]
  def deleteKey(consumerId: UserId): KongService[Unit] = Free.liftF(DeleteKey(consumerId))
  def getKey(consumerId: UserId): KongService[Option[Key]] = Free.liftF(GetKey(consumerId))
}
