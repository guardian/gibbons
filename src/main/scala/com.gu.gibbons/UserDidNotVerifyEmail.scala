package com.gu.gibbons

// ------------------------------------------------------------------------
import cats.Monad
import config.Settings
import java.time.OffsetDateTime
import model._
import services._
// ------------------------------------------------------------------------

/** Deletes keys for users who have been sent a verification email but failed to
 * verify within 24 hours
 *
 * @param email The email service interpreter
 * @param bonobo The bonobo service interpreter
 * @param logger The logging service interpreter
 */
class UserDidNotVerifyEmail[F[_]: Monad](
    settings: Settings,
    email: EmailService[F],
    bonobo: BonoboService[F],
    override val logger: LoggingService[F]
    ) extends Script[F] {

  import cats.syntax.functor._
  import cats.syntax.flatMap._


  def getKeys(now: OffsetDateTime): F[Vector[Key]] = {
    for {
      unverifiedUsers <- bonobo.getUnverifiedUsers(now.minus(Settings.userVerificationGracePeriod).toInstant)
      _ <- logger.info(s"Found ${unverifiedUsers.length} unverified users.")
    } yield unverifiedUsers.map(user => bonobo.getKeysByOwner(user))
  }

  def processKey(now: OffsetDateTime)(key: Key): F[(UserId, Option[EmailResult])] =
    for {
      _ <- bonobo.deleteKey(key)
    } yield key.userId -> None

}
