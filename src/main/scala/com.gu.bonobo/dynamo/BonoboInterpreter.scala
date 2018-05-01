package com.gu.bonobo.dynamo

import java.time.{Instant, OffsetDateTime}
import java.time.temporal.TemporalAmount
import monix.eval.Task
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import com.gu.scanamo._
import com.gu.scanamo.ops.ScanamoOps
import com.gu.scanamo.query.ConditionExpression
import com.gu.scanamo.syntax._

import com.gu.bonobo.config._
import com.gu.bonobo.kong._
import com.gu.bonobo.model._
import com.gu.bonobo.services._

class BonoboInterpreter(config: Settings, kong: KongInterpreter, logger: LoggingService[Task]) extends BonoboService[Task] {
  import cats.syntax.apply._
  import cats.syntax.flatMap._

  def getKeys(period: TemporalAmount) = {
    val jadis = OffsetDateTime.now().minus(period).toInstant.toEpochMilli
    getKeysMatching(period, (attributeExists('extendedOn) and 'extendedOn <= jadis) or (not(attributeExists('extendedOn)) and 'createdOn <= jadis))
  }

  def getKey(keyId: KeyId): Task[Option[Key]] = run {
    keysTable
      .query(hashKeyName -> hashKey and rangeKeyName -> keyId.id)
      .map(_.headOption.collect { case Right(key) => key })
  }

  def getKeyCountFor(userId: UserId) = run {
    keysTable
      .filter('bonoboId -> userId.id)
      .scan()
      .map(_.length)
  }

  def getInactiveKeys(period: TemporalAmount) = {
    val jadis = OffsetDateTime.now().minus(period).toInstant.toEpochMilli
    getKeysMatching(period, attributeExists('remindedOn) and 'remindedOn <= jadis)
  }

  def deleteKey(keyId: KeyId) = getKey(keyId) >>= {
    case Some(key) => deleteKeyInDynamo(key) *> deleteKeyInKong(key)
    case None => Task.now(())
  }

  def setExtendedOn(keyId: KeyId, when: Instant) = updateTime(keyId, 'extendedOn, when.toEpochMilli) 

  def setRemindedOn(keyId: KeyId, when: Instant) = updateTime(keyId, 'remindedOn, when.toEpochMilli) 

  def getUser(userId: UserId) = run {
    usersTable.query('id -> userId.id).map(_.headOption.collect { case Right(user) => user })
  }

  def deleteUser(userId: UserId) = run {
    usersTable.delete('id -> userId.id).map(_ => ())
  }

  private val dynamoClient = AmazonDynamoDBAsyncClientBuilder.standard()
    .withCredentials(config.credentialsProvider)
    .withRegion(config.region)
    .build()

  private val keysTable = Table[Key](config.keys.tableName)
  private val usersTable = Table[User](config.users.tableName)

  private val hashKey = "hashkey"
  private val hashKeyName = 'hashkey
  private val rangeKeyName = 'rangeKey

  private def run[A](program: ScanamoOps[A]) = Task.deferFutureAction { implicit scheduler =>
    ScanamoAsync.exec(dynamoClient)(program)
  }

  private def deleteKeyInDynamo(key: Key) = run {
    keysTable.delete('hashkey -> key.hashKey and 'rangekey -> key.rangeKey)
  }

  private def deleteKeyInKong(key: Key) =
    KongService.deleteKey(key.userId).foldMap(kong)

  private def getKeysMatching[C: ConditionExpression](period: TemporalAmount, filter: C) = run {
    keysTable
      .filter(not('tier -> "Internal") and 'status -> "Active" and filter)
      .scan()
      .map(_.collect { case Right(key) => key }.toVector)
  }

  private def updateTime(keyId: KeyId, col: Symbol, value: Long) = run {
    keysTable.update(hashKeyName -> hashKey and rangeKeyName -> keyId.id, set(col -> value)).map(_ => ())
  }
}