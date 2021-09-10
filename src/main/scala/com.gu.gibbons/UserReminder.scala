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
  import cats.instances.vector._
  import cats.instances.map._
  import cats.syntax.flatMap._
  import cats.syntax.functor._
  import cats.syntax.traverse._
  import cats.syntax.foldable._

  def getUsers(now: OffsetDateTime): F[Vector[User]] =
    for {
      users <- bonobo.getUsers(now.minus(Settings.inactivityPeriod).toInstant)
      developerUsers <- bonobo.getDevelopers(users)
      _ <- logger.info(s"Found ${users.length} users, ${developerUsers.length} are developers. ")
    } yield developerUsers

  def processUser(now: OffsetDateTime)(user: User): F[(UserId, Option[EmailResult])] = {
    val nowL = now.toInstant.toEpochMilli
    for {
      newUser <- bonobo.setRemindedOn(user, nowL)
      res <- email.sendReminder(newUser)
    } yield (newUser.id -> Some(res))
  }
}
