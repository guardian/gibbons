package com.gu.gibbons

import cats.Monad
import java.time.OffsetDateTime
import model._
import services._

abstract class Script[F[_]: Monad] {
  import cats.instances.vector._
  import cats.syntax.flatMap._
  import cats.syntax.functor._
  import cats.syntax.traverse._

  def logger: LoggingService[F]
  
  def run(now: OffsetDateTime, dryRun: Boolean): F[Map[UserId, Option[EmailResult]]] =
    for {
      users <- getUsers(now)
      _ <- logger.info(s"Found ${users.length} developers.")
      ress <- if (dryRun) 
        Monad[F].pure(users.map(_.id -> (None: Option[EmailResult])).toMap) 
      else users.traverse(processUser(now)).map(_.toMap)
      _ <- logger.info("aaaand that's a wrap! See you next time.")
    } yield ress

  def getUsers(now: OffsetDateTime): F[Vector[User]]

  def processUser(now: OffsetDateTime)(user: User): F[(UserId, Option[EmailResult])]

}