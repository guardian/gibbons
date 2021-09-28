package com.gu.gibbons

// ------------------------------------------------------------------------
import cats.Monad
import config.Settings
import java.time.{ OffsetDateTime, ZoneOffset }
import model._
import services._
// ------------------------------------------------------------------------

/** Sends a reminder email to all users who possess a key for at least xx months
 *
 * @param email The email service interpreter
 * @param bonobo The bonobo service interpreter
 * @param logger The logging service interpreter
 */
class UserReminder[F[_]: Monad](
  settings: Settings,
  email: EmailService[F],
  bonobo: BonoboService[F],
  override val logger: LoggingService[F]
) extends Script[F] {
  import cats.syntax.flatMap._
  import cats.syntax.functor._


  def getKeys(now: OffsetDateTime): F[Vector[Key]] =
    for {
      keys <- bonobo.getPotentiallyInactiveDeveloperKeys(now.minus(Settings.inactivityPeriod).toInstant)
      _ <- logger.info(s"Found ${keys.length} potentially inactive developer keys. ")
    } yield keys

  def processKey(now: OffsetDateTime)(key: Key): F[(UserId, Option[EmailResult])] = {
    val nowL = now.toInstant.toEpochMilli
    for {
      potentiallyInactiveKey <- bonobo.setRemindedAt(key, nowL)
      potentiallyInactiveKeyOwner <- bonobo.getKeyOwner(potentiallyInactiveKey)
      res <- email.sendReminder(potentiallyInactiveKeyOwner, potentiallyInactiveKey)
    } yield (potentiallyInactiveKey.userId -> Some(res))
  }
}
