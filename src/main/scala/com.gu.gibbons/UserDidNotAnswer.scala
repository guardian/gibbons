package com.gu.gibbons

// ------------------------------------------------------------------------
import cats.Monad
import config.Settings
import java.time.OffsetDateTime
import model._
import services._
// ------------------------------------------------------------------------

/** Deletes keys for those users who have been sent a reminder but failed to
 * answer with xx days
 *
 * @param email The email service interpreter
 * @param bonobo The bonobo service interpreter
 * @param logger The logging service interpreter
 */
class UserDidNotAnswer[F[_]: Monad](
  settings: Settings,
  email: EmailService[F],
  bonobo: BonoboService[F],
  override val logger: LoggingService[F]
) extends Script[F] {
  import cats.syntax.functor._
  import cats.syntax.flatMap._


  def getKeys(now: OffsetDateTime): F[Vector[Key]] =
    for {
      inactiveKeys <- bonobo.getIgnoredReminderKeys(now.minus(Settings.gracePeriod).toInstant)
      _ <- logger.info(s"Found ${inactiveKeys.length} inactive developer keys. ")
    } yield inactiveKeys

  def processKey(now: OffsetDateTime)(key: Key): F[(UserId, Option[EmailResult])] =
    for {
      _ <- logger.info(s"Key: ${key} ")
      _ <- bonobo.deleteKey(key)
      keyOwner <- bonobo.getKeyOwner(key)
      _ <- logger.info(s"Key Owner: ${keyOwner}")
      res <- email.sendDeleted(keyOwner)
    } yield key.userId -> Some(res)

}
