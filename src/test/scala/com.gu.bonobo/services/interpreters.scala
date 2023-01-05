package com.gu.gibbons
package services

import cats.Monad
import cats.data.State
import java.time.Instant
import model._

class BonoboServiceInterpreter extends BonoboService[TestProgram] {
  import cats.implicits._

  def getPotentiallyInactiveDeveloperKeys(createdBefore: Instant): TestProgram[Vector[Key]] =
    State.get.map(_._3.filter {
      case (_, key) => createdBefore.toEpochMilli >= key.extendedAt.getOrElse(key.createdAt)
    }.values.toVector)

  def getIgnoredReminderKeys(remindedBefore: Instant): TestProgram[Vector[Key]] =
    State.get.map(_._3.filter {
      case (_, key) => remindedBefore.toEpochMilli >= key.remindedAt.getOrElse(key.createdAt)
    }.values.toVector)

  def getUnverifiedUsers(verificationSentBefore: Instant): TestProgram[Vector[User]] =
    State.get.map(_._1.filter {
      case (_, user) => verificationSentBefore.toEpochMilli >= user.verificationSentAt.getOrElse(user.createdAt)
    }.values.toVector)

  def getKeysByOwner(user: User): TestProgram[Vector[Key]] =
    State.get.map(_._3.filter {
      case (_, key) => key.userId == user.id
    }.values.toVector)

  def getKeyOwner(key: Key): TestProgram[User] =
    State.get.map(_._1.filter {
      case (_, user) => user.id == key.userId
    }.values.toVector.head)

  def setRemindedAt(key: Key, when: Long): TestProgram[Key] =
    State.get.flatMap { case (users, emails, keys) =>
      val newKey = keys(key.consumerId).copy(remindedAt = Some(when))
      for {
        _ <- State.set((users, emails, keys.updated(key.consumerId, newKey)))
      } yield newKey
    }

  def deleteKey(key: Key): TestProgram[Unit] = for {
    s <- State.get
    _ <- State.set((s._1, s._2, s._3 - key.consumerId))
  } yield ()

  def deleteUnverifiedUserAndKeys(user: User): TestProgram[Unit] = ???
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
  private def result(header: String, user: User) = EmailResult(
    """${header}
      |${user.email.email}""".stripMargin
  )

  def sendReminder(user: User, key: Key): TestProgram[EmailResult] = for {
    s <- State.get
    _ <- State.set((s._1, s._2 + user.email, s._3))
  } yield result("Reminder email", user)
  
  def sendDeleted(user: User): TestProgram[EmailResult] = for {
    s <- State.get
    _ <- State.set((s._1, s._2 + user.email, s._3))
  } yield result("Deletion email", user)
}