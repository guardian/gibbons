package com.gu.gibbons
package services

import cats.Monad
import cats.data.State
import java.time.Instant
import model._

class BonoboServiceInterpreter extends BonoboService[TestProgram] {
  def getUsers(jadis: Instant) = State[Repo, Vector[User]] { case s@(users, _) =>
    val res = users.filter { 
      case (_, user) => user.extendedAt.getOrElse(user.createdAt).compareTo(jadis) <= 0
    }.values.toVector
    (s, res)
  }

  def getInactiveUsers(jadis: Instant) = State[Repo, Vector[User]] { case s@(users, _) =>
    val res = users.filter { 
      case (_, user) => user.remindedAt.exists(r => r.compareTo(jadis) <= 0)
    }.values.toVector
    (s, res)
  }

  def setRemindedOn(user: User, when: Instant) = State[Repo, User] { case s@(users, es) =>
    val newUser = users(user.id).copy(remindedAt = Some(when))
    ((users.updated(user.id, newUser), es), newUser)
  }

  def deleteUser(user: User) = State[Repo, Unit] { case (users, es) =>
    ((users - user.id, es), ())
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
  private def result(header: String, user: User) = EmailResult(
    """${header}
      |${user.email.email}""".stripMargin
  )

  def sendReminder(user: User) = State[Repo, EmailResult] { case (us, emails) =>
    ((us, emails + user.email), result("Reminder email", user))
  }
  
  def sendDeleted(user: User) = State[Repo, EmailResult] { case (us, emails) =>
    ((us, emails + user.email), result("Deletion email", user))
  }
}