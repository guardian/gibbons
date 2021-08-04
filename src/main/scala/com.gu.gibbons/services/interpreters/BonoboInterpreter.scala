package com.gu.gibbons.services.interpreters

import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.traverse._
import cats.syntax.show._
import cats.instances.list._
import cats.instances.vector._
import java.time.Instant
import monix.eval.Task
import okhttp3.{ OkHttpClient, Request }
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.gu.scanamo._
import com.gu.scanamo.ops.ScanamoOps
import com.gu.scanamo.query.{ AndCondition, ConditionExpression }
import com.gu.scanamo.syntax._

import com.gu.gibbons.config._
import com.gu.gibbons.model._
import com.gu.gibbons.services._
import com.gu.gibbons.utils._

class BonoboInterpreter(config: Settings,
                        logger: LoggingService[Task],
                        dynamoClient: AmazonDynamoDBAsync,
                        httpClient: OkHttpClient,
                        urlGenerator: UrlGenerator)
    extends BonoboService[Task] {

  def getUsers(jadis: Instant) =
    for {
      _ <- logger.info(s"Getting all the users created $jadis ago")
      millis = jadis.toEpochMilli
      users <- getItems(usersTable,
                        not(attributeExists('remindedAt)) and ('extendedAt <= millis or (not(
                          attributeExists('extendedAt)
                        ) and 'createdAt <= millis)))
    } yield users

  def getDevelopers(users: Vector[User]) = {
    val uss = users.grouped(99).toVector
    for {
      devKeys <- uss.foldMapM(
        us =>
          getItems(keysTable, AndCondition('bonoboId -> us.map(u => UserId.unwrap(u.id)).toSet, 'tier -> "Developer"))
      )
      nonDevKeys <- uss.foldMapM(
        us =>
          getItems(keysTable, AndCondition('bonoboId -> us.map(u => UserId.unwrap(u.id)).toSet, 'tier -> "External"))
      )
      devUserIds = devKeys.map(_.userId).toSet
      nonDevUserIds = nonDevKeys.map(_.userId).toSet
      onlyDevelopers = devUserIds.diff(nonDevUserIds)
    } yield users.filter(u => onlyDevelopers(u.id))
  }

  def getInactiveUsers(jadis: Instant) =
    for {
      _ <- logger.info(s"Getting all the users reminded $jadis ago")
      millis = jadis.toEpochMilli
      users <- getItems(usersTable, attributeExists('remindedAt) and 'remindedAt <= millis)
    } yield users

  def setRemindedOn(user: User, when: Long) =
    run {
      usersTable.update('id -> UserId.unwrap(user.id), set('remindedAt -> when)).map(_ => ())
    }.map { _ =>
      user.copy(remindedAt = Some(when))
    }

  def deleteUser(user: User) =
    Task.delay {
      val request = new Request.Builder()
        .url(urlGenerator.delete(user))
        .build
      httpClient.newCall(request).execute()
    }.bracket { response =>
      response.code match {
        case 200 => Task(())
        case _   => Task.raiseError(new Throwable(s"Call to Bonobo failed with ${response.message}: ${response.body}"))
      }
    } { response =>
      Task {
        response.close()
      }
    }

  private val usersTable = Table[User](config.usersTableName)

  private val keysTable = Table[Key](config.keysTableName)

  private def run[A](program: ScanamoOps[A]): Task[A] = Task.deferFutureAction { implicit scheduler =>
    ScanamoAsync.exec(dynamoClient)(program)
  }

  private def getItems[A, C: ConditionExpression](table: Table[A], filter: C): Task[Vector[A]] =
    run {
      table.filter(filter).scan()
    }.flatMap { results =>
      for {
        _ <- results.collect { case Left(error) => error }.traverse(error => logger.warn(error.show))
      } yield results.collect { case Right(a) => a }.toVector
    }
}
