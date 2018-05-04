package com.gu.gibbons.dynamo

import java.time.{Instant, OffsetDateTime}
import java.time.temporal.TemporalAmount
import monix.eval.Task
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsyncClientBuilder, AmazonDynamoDBAsync}
import com.gu.scanamo._
import com.gu.scanamo.ops.ScanamoOps
import com.gu.scanamo.query.ConditionExpression
import com.gu.scanamo.syntax._

import com.gu.gibbons.config._
import com.gu.gibbons.kong._
import com.gu.gibbons.model._
import com.gu.gibbons.services._

class BonoboInterpreter(config: Settings, kong: KongInterpreter, logger: LoggingService[Task], dynamoClient: AmazonDynamoDBAsync) extends BonoboService[Task] {
  import cats.syntax.apply._
  import cats.syntax.flatMap._

  def getKeys(period: TemporalAmount) = {
    val jadis = OffsetDateTime.now().minus(period).toInstant.toEpochMilli
    for {
      _ <- logger.info(s"Getting all the keys created before $jadis")
      keys <- getKeysMatching(period, (not(attributeExists('extendedAt)) and 'createdAt <= jadis) or (attributeExists('extendedAt) and 'extendedAt <= jadis))
    } yield keys.collect { case Right(key) => key }.toVector
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
    for {
      _ <- logger.info(s"Getting all the keys created before $jadis")
      keys <- getKeysMatching(period, attributeExists('remindedAt) and 'remindedAt <= jadis)
    } yield keys.collect { case Right(key) => key }.toVector
  }

  def deleteKey(key: Key) = getKey(key.rangeKey) >>= {
    case Some(key) => deleteKeyInDynamo(key) *> deleteKeyInKong(key)
    case None => Task.now(())
  }

  def setExtendedOn(key: Key, when: Instant) = updateTime(key, 'extendedAt, when.toEpochMilli) 

  def setRemindedOn(key: Key, when: Instant) = updateTime(key, 'remindedAt, when.toEpochMilli) 

  def getUser(userId: UserId) = run {
    usersTable.query('id -> userId.id).map(_.headOption.collect { case Right(user) => user })
  }

  def deleteUser(userId: UserId) = run {
    usersTable.delete('id -> userId.id).map(_ => ())
  }

  private val keysTable = Table[Key](config.keys.tableName)
  private val usersTable = Table[User](config.users.tableName)

  private val hashKey = "hashkey"
  private val hashKeyName = 'hashkey
  private val rangeKeyName = 'rangekey

  private def run[A](program: ScanamoOps[A]) = Task.deferFutureAction { implicit scheduler => 
    ScanamoAsync.exec(dynamoClient)(program) 
  }

  private def deleteKeyInDynamo(key: Key) = run {
    keysTable.delete('hashkey -> key.hashKey and 'rangekey -> key.rangeKey)
  }

  private def deleteKeyInKong(key: Key) =
    KongService.deleteKey(key.kongId).foldMap(kong)

  private def getKeysMatching[C: ConditionExpression](period: TemporalAmount, filter: C) = run {
    keysTable
      .filter(not('tier -> "Internal") and 'status -> "Active" and filter)
      .scan()
  }

  private def updateTime(key: Key, col: Symbol, value: Long) = run {
    keysTable.update(hashKeyName -> hashKey and rangeKeyName -> key.rangeKey.id, set(col -> value)).map(_ => ())
  }
}

object BonoboInterpreter {
  def apply(config: Settings, kong: KongInterpreter, logger: LoggingService[Task]): Task[BonoboInterpreter] = Task.evalOnce {
    val dynamoClient = AmazonDynamoDBAsyncClientBuilder.standard()
      .withRegion(config.region)
      .build()

    new BonoboInterpreter(config, kong, logger, dynamoClient)
  }
}