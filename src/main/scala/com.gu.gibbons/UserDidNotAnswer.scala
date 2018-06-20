package com.gu.gibbons

// ------------------------------------------------------------------------
import cats.{ Parallel, Monad }
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
class UserDidNotAnswer[F[_] : Monad, G[_]](
  settings: Settings, 
  email: EmailService[F], 
  bonobo: BonoboService[F], 
  override val logger: LoggingService[F]
)(implicit P: Parallel[F, G]) extends Script[F, G] {
    import cats.instances.vector._
    import cats.syntax.functor._
    import cats.syntax.flatMap._
    import cats.syntax.traverse._

    def getUsers(now: OffsetDateTime): F[Vector[User]] =
      for {
        _ <- logger.info(s"Getting all the users which have not extended their account since ${Settings.gracePeriod}")
        users <- bonobo.getInactiveUsers(now.minus(Settings.gracePeriod).toInstant)
      } yield users

    def processUser(now: OffsetDateTime)(user: User): F[(UserId, Option[EmailResult])] =
      for {
        _ <- bonobo.deleteUser(user)
        res <- email.sendDeleted(user)
      } yield user.id -> Some(res)

}