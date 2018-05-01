package com.gu.bonobo.dynamo

import java.time.{Instant, OffsetDateTime}
import java.time.temporal.TemporalAmount
import monix.eval.Task
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import com.gu.scanamo._
import com.gu.scanamo.ops.ScanamoOps
import com.gu.scanamo.syntax._

import com.gu.bonobo.config._
import com.gu.bonobo.kong._
import com.gu.bonobo.model._
import com.gu.bonobo.services._

class BonoboInterpreter(config: Settings, kong: KongInterpreter, logger: LoggingService[Task]) extends BonoboService[Task] {
  import cats.syntax.apply._

  def getKeys(period: TemporalAmount) = run {
    val jadis = OffsetDateTime.now().minus(period).toInstant.toEpochMilli
    keysTable
      .filter(
        not('tier -> "Internal") and
        'status -> "Active" and
        ((attributeExists('extendedOn) and 'extendedOn <= jadis) or (not(attributeExists('extendedOn)) and 'createdOn <= jadis))
      )
      .scan()
      .map(_.collect { case Right(bKey) => bKey.toKey }.toVector)
  }

  def getKey(keyId: KeyId): Task[Option[Key]] = run {
    keysTable
      .filter('keyValue -> keyId.id)
      .scan()
      .map(_.headOption.collect { case Right(bKey) => bKey.toKey })
  }

  def getKeyCountFor(userId: UserId) = run {
    keysTable
      .filter('kongConsumerId -> userId.id)
      .scan()
      .map(_.length)
  }

  def getInactiveKeys(period: TemporalAmount) = run {
    val jadis = OffsetDateTime.now().minus(period).toInstant.toEpochMilli
    keysTable
      .filter(
        not('tier -> "Internal") and
        'status -> "Active" and
        attributeExists('remindedOn) and 
        'remindedOn <= jadis
      )
      .scan()
      .map(_.collect { case Right(bKey) => bKey.toKey }.toVector)
  }

  def deleteKey(keyId: KeyId) = for {
    key <- getKey(keyId)
    _ <- key.fold(Task.now(()))(key => deleteKeyInDynamo(key) *> deleteKeyInKong(key))
  } yield ()

  def setExtendedOn(keyId: KeyId, when: Instant) = run {
    keysTable.update('keyValue -> keyId.id, set('extendedOn -> when.toEpochMilli)).map(_ => ())
  }

  def setRemindedOn(keyId: KeyId, when: Instant) = run {
    keysTable.update('keyValue -> keyId.id, set('remindedOn -> when.toEpochMilli)).map(_ => ())
  }

  def getUser(userId: UserId) = run {
    usersTable.query('id -> userId.id).map(_.headOption.collect { case Right(kUser) => kUser.toUser })
  }

  def deleteUser(userId: UserId) = run {
    usersTable.delete('id -> userId.id).map(_ => ())
  }

  private val dynamoClient = AmazonDynamoDBAsyncClientBuilder.standard()
    .withCredentials(config.credentialsProvider)
    .withRegion(config.region)
    .build()

  private val keysTable = Table[BonoboKey](config.keys.tableName)
  private val usersTable = Table[BonoboUser](config.users.tableName)

  private def run[A](program: ScanamoOps[A]) = Task.deferFutureAction { implicit scheduler =>
    ScanamoAsync.exec(dynamoClient)(program)
  }

  private def deleteKeyInDynamo(key: Key) = run {
    keysTable.delete('hashkey -> key.hashKey and 'rangekey -> key.rangeKey)
  }

  private def deleteKeyInKong(key: Key) =
    KongService.deleteKey(key.userId).foldMap(kong)
}