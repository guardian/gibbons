package com.gu.gibbons

// ------------------------------------------------------------------------
import cats.Monad
import config.Settings
import java.time.OffsetDateTime
import model._
import services._
// ------------------------------------------------------------------------

/** Deletes users who have been sent a verification email but failed to
 * verify within 24 hours and their keys
 *
 * @param email The email service interpreter
 * @param bonobo The bonobo service interpreter
 * @param logger The logging service interpreter
 */
class UnverifiedUser[F[_]: Monad](
    settings: Settings,
    email: EmailService[F],
    bonobo: BonoboService[F],
    override val logger: LoggingService[F]
    ) extends Script[F] {

  import cats.syntax.functor._
  import cats.syntax.flatMap._
  import cats.instances.vector._
  import cats.syntax.traverse._

  def getKeys(now: OffsetDateTime): F[Vector[Key]] = {
    for {
      unverifiedUsers <- bonobo.getUnverifiedUsers(now.minus(Settings.verificationGracePeriod).toInstant)
      _ <- logger.info(s"Found ${unverifiedUsers.length} unverified users.")
      unverifiedUserKeys <- unverifiedUsers.traverse(bonobo.getKeysByOwner(_))
    } yield unverifiedUserKeys.flatten
  }

  def processKey(now: OffsetDateTime)(key: Key): F[(UserId, Option[EmailResult])] =
    for {
      owner <- bonobo.getKeyOwner(key)
      _ <- bonobo.deleteUnverifiedUserAndKeys(owner)
    } yield key.userId -> None

}
