package com.gu.gibbons.services.interpreters

import java.time.{Instant, OffsetDateTime}
import java.time.temporal.TemporalAmount
import monix.eval.Task
import okhttp3.{OkHttpClient, Request}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsyncClientBuilder, AmazonDynamoDBAsync}
import com.gu.scanamo._
import com.gu.scanamo.ops.ScanamoOps
import com.gu.scanamo.query.ConditionExpression
import com.gu.scanamo.syntax._

import com.gu.gibbons.config._
import com.gu.gibbons.model._
import com.gu.gibbons.services._

class BonoboInterpreter(config: Settings, logger: LoggingService[Task], dynamoClient: AmazonDynamoDBAsync, httpClient: OkHttpClient) extends BonoboService[Task] {
  import cats.syntax.apply._
  import cats.syntax.flatMap._

  def getUsers(period: TemporalAmount) = {
    val jadis = OffsetDateTime.now().minus(period).toInstant.toEpochMilli
    for {
      _ <- logger.info(s"Getting all the users created before $jadis")
      users <- getUsersMatching(period, ('extendedAt <= jadis or (not(attributeExists('extendedAt)) and 'createdAt <= jadis)))
    } yield users.collect { case Right(user) => user }.toVector
  }

  def getInactiveUsers(period: TemporalAmount) = {
    val jadis = OffsetDateTime.now().minus(period).toInstant.toEpochMilli
    for {
      _ <- logger.info(s"Getting all the users created before $jadis")
      users <- getUsersMatching(period, attributeExists('remindedAt) and 'remindedAt <= jadis)
    } yield users.collect { case Right(user) => user }.toVector
  }

  def setRemindedOn(user: User, when: Instant) = run {
    usersTable.update('bonoboId -> user.id.id, set('remindedAt -> when.toEpochMilli)).map(_ => ())
  }

  def deleteUser(user: User) = Task {
    val request = new Request.Builder()
      .url(urlGenerator.delete(user))
      .build
    httpClient.newCall(request).execute()
    ()
  }

  private val urlGenerator = new UrlGenerator(config)

  private val usersTable = Table[User](config.usersTableName)

  private def run[A](program: ScanamoOps[A]) = Task.deferFutureAction { implicit scheduler => 
    ScanamoAsync.exec(dynamoClient)(program) 
  }

  private def getUsersMatching[C: ConditionExpression](period: TemporalAmount, filter: C) = run {
    usersTable
      .filter(filter)
      .scan()
  }
}

object BonoboInterpreter {
  def apply(config: Settings, logger: LoggingService[Task]): Task[BonoboInterpreter] = Task.evalOnce {
    val dynamoClient = AmazonDynamoDBAsyncClientBuilder.standard()
      .withRegion(config.region)
      .build()

    val httpClient = new OkHttpClient()

    new BonoboInterpreter(config, logger, dynamoClient, httpClient)
  }
}