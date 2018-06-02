package com.gu.gibbons
package services

import cats.Monad
import cats.data.State
import java.time.Instant
import java.time.temporal.TemporalAmount
import model._

class BonoboServiceInterpreter extends BonoboService[TestProgram] {
  import cats.implicits._

  def getUsers(period: TemporalAmount): TestProgram[Vector[User]] = 
    State.get.map(_._1.filter { 
      case (_, user) => fixtures.today.minus(period).toInstant.compareTo(user.extendedAt.getOrElse(user.createdAt)) >= 0
    }.values.toVector)

  def getInactiveUsers(period: TemporalAmount): TestProgram[Vector[User]] = 
    State.get.map(_._1.filter { 
      case (_, user) => user.remindedAt.exists(r => fixtures.today.minus(period).toInstant.compareTo(r) >= 0)
    }.values.toVector)

  def setRemindedOn(user: User, when: Instant): TestProgram[User] = 
    State.get.flatMap { case (users, emails) =>
      val newUser = users(user.id).copy(remindedAt = Some(when))
      for {
        _ <- State.set((users.updated(user.id, newUser), emails))
      } yield newUser
    }

  def deleteUser(user: User): TestProgram[Unit] = for {
    s <- State.get
    _ <- State.set((s._1 - user.id, s._2))
  } yield ()
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

  def sendReminder(user: User): TestProgram[EmailResult] = for { 
    s <- State.get
    _ <- State.set((s._1, s._2 + user.email))
  } yield result("Reminder email", user)
  
  def sendDeleted(user: User): TestProgram[EmailResult] = for {
    s <- State.get
    _ <- State.set((s._1, s._2 + user.email))
  } yield result("Deletion email", user)
}