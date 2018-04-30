package com.gu.bonobo.dynamo

import java.time.Instant
import java.time.temporal.TemporalAmount
import monix.eval.Task
import com.gu.scanamo._

import com.gu.bonobo.config._
import com.gu.bonobo.kong._
import com.gu.bonobo.model._
import com.gu.bonobo.services.BonoboService

class DynamoBonoboService[F[_]](config: Settings) extends BonoboService[F] {
    val keysTable = Table[BonoboKey](config.keys.tableName)
    val usersTableName = Table[BonoboUser](config.users.tableName)

    def getKeys(period: TemporalAmount) = ???

    def getKey(keyId: KeyId): Task[Option[Key]] = ???

    def getKeyCountFor(userId: UserId) = ???

    def getInactiveKeys(period: TemporalAmount) = ???

    def deleteKey(keyId: KeyId) = ???

    def setExtendedOn(keyId: KeyId, when: Instant) = ???

    def setRemindedOn(keyId: KeyId, when: Instant) = ???

    def getUser(userId: UserId) = ???

    def deleteUser(userId: UserId) = ???
    
}

class BonoboInterpreter(config: Settings, kong: KongInterpreter) extends DynamoBonoboService[Task](config) {
    def getKeys(period: TemporalAmount) = ???

    def getKey(keyId: KeyId): Task[Option[Key]] = Task.now(None)

    def getKeyCountFor(userId: UserId) = ???

    def getInactiveKeys(period: TemporalAmount) = ???

    def deleteKey(keyId: KeyId) = for {
        key <- getKey(keyId)
        _ <- key.fold(Task.now(()))(key => KongService.deleteKey(key.userId).foldMap(kong))
    } yield ()

    def setExtendedOn(keyId: KeyId, when: Instant) = ???

    def setRemindedOn(keyId: KeyId, when: Instant) = ???

    def getUser(userId: UserId) = ???

    def deleteUser(userId: UserId) = ???

}