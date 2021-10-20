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
    extends BonoboService[Task]{


  def getPotentiallyInactiveDeveloperKeys(createdBefore: Instant) =
    for {
      _ <- logger.info(s"Getting all developer keys created before $createdBefore")
      millis = createdBefore.toEpochMilli
      keys <- getItems(keysTable, ('createdAt <= millis) and
        ('tier, "Developer") and
        not(attributeExists('remindedAt)) and
        (not(attributeExists('extendedAt)) or ('extendedAt <= millis))
      )
    } yield keys

  def getIgnoredReminderKeys(reminderDate: Instant) =
    for {
      _ <- logger.info(s"Getting all the keys whose owners were reminded since $reminderDate")
      millis = reminderDate.toEpochMilli
      keys <- getItems(keysTable, attributeExists('remindedAt) and 'remindedAt <= millis)
    } yield keys

  def setRemindedAt(key: Key, when: Long) =
    run {
      keysTable.update('hashkey -> "hashkey" and 'rangekey -> key.rangeKey, set('remindedAt -> when)).map(_ => ())
    }.map { _ =>
      key.copy(remindedAt = Some(when))
    }

  def getKeyOwner(key: Key) = {
    for {
      user <- getItems(usersTable, 'id ->  UserId.unwrap(key.userId))
    } yield user.toList.head
  }

  def deleteKey(key: Key) =
    Task.delay {
      val request = new Request.Builder()
        .url(urlGenerator.deleteKey(key))
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

  private val keysTable = Table[Key](config.keysTableName)

  private val usersTable = Table[User](config.usersTableName)

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
