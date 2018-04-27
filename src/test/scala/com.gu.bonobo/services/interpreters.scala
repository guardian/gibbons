package com.gu.bonobo
package services

import cats.Monad
import cats.data.State
import java.time.Instant
import java.time.temporal.TemporalAmount
import model._

class BonoboServiceInterpreter extends BonoboService[TestProgram] {
  def getKeys(period: TemporalAmount) = State[Repo, Vector[Key]] { case s@(keys: KeyRepo, _) =>
    val res = keys.filter { 
      case (_, key) => fixtures.today.minus(period).toInstant.compareTo(key.extendedOn.getOrElse(key.createdOn)) >= 0
    }.values.toVector
    (s, res)
  }

  def getKey(keyId: KeyId) = State[Repo, Option[Key]] { case s@(keys, _) =>
    (s, keys.get(keyId))
  }

  def getInactiveKeys(period: TemporalAmount) = State[Repo, Vector[Key]] { case s@(keys, _) =>
    val res = keys.filter { 
      case (_, key) => key.remindedOn.exists { t => fixtures.today.minus(period).toInstant.compareTo(t) >= 0 }
    }.values.toVector
    (s, res)
  }

  def deleteKey(keyId: KeyId) = State[Repo, Unit] { case (keys, users) =>
    val newKeys = keys - keyId
    ((newKeys, users), ())
  }

  def setExtendedOn(keyId: KeyId, when: Instant) = State[Repo, Unit] { case (keys, users) =>
    val newKeys = for {
      key <- keys.get(keyId)
    } yield {
      val newKey = key.copy(extendedOn = Some(when))
      keys.updated(keyId, newKey)
    }
    
    ((newKeys.getOrElse(keys), users), ())
  }

  def setRemindedOn(keyId: KeyId, when: Instant) = State[Repo, Unit] { case (keys, users) =>
    val newKeys = for {
      key <- keys.get(keyId)
    } yield {
      val newKey = key.copy(remindedOn = Some(when))
      keys.updated(keyId, newKey)
    }
    
    ((newKeys.getOrElse(keys), users), ())
  }

  def getUser(userId: UserId) = State[Repo, Option[User]] { case s@(_, users) =>
    (s, users.get(userId))
  }

  def deleteUser(userId: UserId) = State[Repo, Unit] { case (keys, users) =>
    ((keys, users - userId), ())
  }
}

class LoggingServiceInterpreter extends LoggingService[TestProgram] {
  def info(msg: String) = {
    println(s"[INFO] $msg")
    Monad[TestProgram].pure(())
  }

  def warn(msg: String) = {
    println(s"[WARN] $msg")
    Monad[TestProgram].pure(())
  }

  def error(msg: String) = {
    println(s"[ERROR] $msg")
    Monad[TestProgram].pure(())
  }
}

class EmailServiceInterpreter extends EmailService[TestProgram] {
  private def result(header: String, origin: Email, destination: Destination, keys: Vector[Key]) = EmailResult(
    """${header}
      |${origin.email}
      |${destination.to.email}
      |${keys.mkString(",")}""".stripMargin
  )

  def sendReminder(origin: Email, destination: Destination, keys: Vector[Key]) = Monad[TestProgram].pure(result("Reminder email", origin, destination, keys))
  
  def sendDeleted(origin: Email, destination: Destination, keys: Vector[Key]) = Monad[TestProgram].pure(result("Deletion email", origin, destination, keys))
}