package com.gu.gibbons.services.interpreters

import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import cats.syntax.show._
import cats.instances.list._
import java.time.{Instant, OffsetDateTime}
import java.time.temporal.TemporalAmount
import monix.eval.Task
import okhttp3.{OkHttpClient, Request}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.gu.scanamo._
import com.gu.scanamo.ops.ScanamoOps
import com.gu.scanamo.query.ConditionExpression
import com.gu.scanamo.syntax._

import com.gu.gibbons.config._
import com.gu.gibbons.model._
import com.gu.gibbons.services._
import com.gu.gibbons.utils._

class BonoboInterpreter(config: Settings, logger: LoggingService[Task], dynamoClient: AmazonDynamoDBAsync, httpClient: OkHttpClient, urlGenerator: UrlGenerator) extends BonoboService[Task] {

  def getUsers(period: TemporalAmount) = for {
    jadis <- timeAgo(period)
    millis = jadis.toEpochMilli
    _ <- logger.info(s"Getting all the users created $period ago")
    users <- getUsersMatching(not(attributeExists('remindedAt)) and ('extendedAt <= millis or (not(attributeExists('extendedAt)) and 'createdAt <= millis)))
  } yield users

  def getInactiveUsers(period: TemporalAmount) = for {
    jadis <- timeAgo(period)
    millis = jadis.toEpochMilli
    _ <- logger.info(s"Getting all the users reminded $period ago")
    users <- getUsersMatching(attributeExists('remindedAt) and 'remindedAt <= millis)
  } yield users

  def setRemindedOn(user: User, when: Instant) = run {
    usersTable.update('id -> user.id.id, set('remindedAt -> when.toEpochMilli)).map(_ => ())
  }.map { _ => user.copy(remindedAt = Some(when)) }

  def deleteUser(user: User) = Task {
    val request = new Request.Builder()
      .url(urlGenerator.delete(user))
      .build
    val response = httpClient.newCall(request).execute()
    response.code match {
      case 200 => ()
      case _ => throw new Throwable(s"Call to Bonobo failed with ${response.message}: ${response.body}")
    }
  }

  private val usersTable = Table[User](config.usersTableName)

  private def timeAgo(period: TemporalAmount) = Task.eval { 
    OffsetDateTime.now().minus(period).toInstant
  }

  private def run[A](program: ScanamoOps[A]): Task[A] = Task.deferFutureAction { implicit scheduler => 
    ScanamoAsync.exec(dynamoClient)(program) 
  }

  private def getUsersMatching[C: ConditionExpression](filter: C): Task[Vector[User]] = run {
    usersTable
      .filter(filter)
      .scan()
  }.flatMap { results =>
    for {
      _ <- results.collect { case Left(error) => error }.traverse(error => logger.warn(error.show))
    } yield results.collect { case Right(user) => user }.toVector
  }
}
