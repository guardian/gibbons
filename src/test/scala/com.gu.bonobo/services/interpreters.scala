package com.gu.gibbons
package services

import cats.Monad
import cats.data.State
import java.time.Instant
import java.time.temporal.TemporalAmount
import model._

class BonoboServiceInterpreter extends BonoboService[TestProgram] {
  def getKeys(period: TemporalAmount) = State[Repo, Vector[Key]] { case s@(keys, _, _) =>
    val res = keys.filter { 
      case (_, key) => fixtures.today.minus(period).toInstant.compareTo(key.extendedOn.getOrElse(key.createdOn)) >= 0
    }.values.toVector
    (s, res)
  }

  def getKey(keyId: KeyId) = State[Repo, Option[Key]] { case s@(keys, _, _) =>
    (s, keys.get(keyId))
  }

  def getKeyCountFor(userId: UserId) = State[Repo, Int] { case s@(keys, _, _) =>
    (s, keys.values.filter(_.userId == userId).size)
  }

  def getInactiveKeys(period: TemporalAmount) = State[Repo, Vector[Key]] { case s@(keys, _, _) =>
    val res = keys.filter { 
      case (_, key) => key.remindedOn.exists { t => fixtures.today.minus(period).toInstant.compareTo(t) >= 0 }
    }.values.toVector
    (s, res)
  }

  def deleteKey(key: Key) = State[Repo, Unit] { case (keys, users, es) =>
    val newKeys = keys - key.rangeKey
    ((newKeys, users, es), ())
  }

  def setExtendedOn(key: Key, when: Instant) = State[Repo, Unit] { case (keys, users, es) =>
    val newKeys = keys.updated(key.rangeKey, key.copy(extendedOn = Some(when)))
    ((newKeys, users, es), ())
  }

  def setRemindedOn(key: Key, when: Instant) = State[Repo, Unit] { case (keys, users, es) =>
    val newKeys = keys.updated(key.rangeKey, key.copy(remindedOn = Some(when)))
    ((newKeys, users, es), ())
  }

  def getUser(userId: UserId) = State[Repo, Option[User]] { case s@(_, users, _) =>
    (s, users.get(userId))
  }

  def deleteUser(userId: UserId) = State[Repo, Unit] { case (keys, users, es) =>
    ((keys, users - userId, es), ())
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
  private def result(header: String, user: User, keys: Vector[Key]) = EmailResult(
    """${header}
      |${user.email.email}
      |${keys.mkString(",")}""".stripMargin
  )

  def sendReminder(user: User, keys: Vector[Key]) = State[Repo, EmailResult] { case (ks, us, emails) =>
    ((ks, us, emails + user.email), result("Reminder email", user, keys))
  }
  
  def sendDeleted(user: User, keys: Vector[Key]) = State[Repo, EmailResult] { case (ks, us, emails) =>
    ((ks, us, emails + user.email), result("Deletion email", user, keys))
  }
}