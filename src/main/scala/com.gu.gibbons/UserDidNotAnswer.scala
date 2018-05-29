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
class UserDidNotAnswer[F[_] : Monad](settings: Settings, email: EmailService[F], bonobo: BonoboService[F], logger: LoggingService[F]) extends Script[F] {
    import cats.instances.vector._
    import cats.syntax.functor._
    import cats.syntax.flatMap._
    import cats.syntax.traverse._

    /** The whole program:
      * 1- Finds out the keys for which a reminder has been sent
      * 2- Look up who's behind them
      * 3- Delete those keys
      * 4- Send an email to inform users their keys have been deleted
      */
    def run(now: OffsetDateTime, dryRun: Boolean): F[Result] = {
      val fromBefore = now.minus(Settings.gracePeriod).toInstant

      if (dryRun)
        for {
          _ <- logger.info(s"Getting all the users which have not been extended since ${Settings.gracePeriod}")
          users <- bonobo.getInactiveUsers(fromBefore)
        } yield DryRun(users)
      else
        for {
          _ <- logger.info(s"Getting all the users which have not extended their account since ${Settings.gracePeriod}")
          users <- bonobo.getInactiveUsers(fromBefore)
          _ <- logger.info(s"Found ${users.length} users. Let's delete these keys...")
          ress <- users.filterNot(u => Settings.whitelist(u.id.id)).traverse { user =>
            for {
              _ <- bonobo.deleteUser(user)
              res <- email.sendDeleted(user)
            } yield user.id -> res
          }.map(_.toMap)
          _ <- logger.info("That's a wrap! See ya.")
        } yield FullRun(ress)
    }
}