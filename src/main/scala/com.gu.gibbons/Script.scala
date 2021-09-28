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
      keys <- getKeys(now)
      _ <- logger.info(s"Dry run mode: ${dryRun}")
      _ <- logger.info(s"Developer key owners: ${keys.map(_.userId).mkString(", ")}")
      ress <- if (dryRun) {
        Monad[F].pure(keys.map(_.userId -> (None: Option[EmailResult])).toMap)

      } else keys.traverse(processKey(now)).map(_.toMap)
      _ <- logger.info("aaaand that's a wrap! See you next time.")
    } yield ress

  def getKeys(now: OffsetDateTime): F[Vector[Key]]

  def processKey(now: OffsetDateTime)(key: Key): F[(UserId, Option[EmailResult])]

}
