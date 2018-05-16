package com.gu.gibbons

// ------------------------------------------------------------------------
import cats.Monad
import config.Settings
import java.time.Instant
import model._
import services._
// ------------------------------------------------------------------------

/** Sends a reminder email to all users who possess a key for at least xx months
  *
  * @param email The email service interpreter
  * @param bonobo The bonobo service interpreter
  * @param logger The logging service interpreter
  */
class UserReminder[F[_] : Monad](settings: Settings, email: EmailService[F], bonobo: BonoboService[F], logger: LoggingService[F]) {
    import cats.instances.vector._
    import cats.instances.map._
    import cats.syntax.flatMap._
    import cats.syntax.functor._
    import cats.syntax.traverse._

    /** The whole program:
      * 1- Finds out the keys which are older than xx months
      * 2- Look up who's behind them
      * 3- Sends a reminder email to each user 
      * 4- Update keys to log when a reminder has been sent
      */
    def run(now: Instant, dryRun: Boolean): F[Result] = 
      if (dryRun) 
        for {
          _ <- logger.info("Yop, who's up for receiving a reminder?")
          users <- bonobo.getUsers(Settings.inactivityPeriod)
        } yield DryRun(users)
      else
        for {
          _ <- logger.info(s"Getting all the users older than ${Settings.inactivityPeriod}")
          users <- bonobo.getUsers(Settings.inactivityPeriod)
          _ <- logger.info(s"Found ${users.length} users. Let's send some emails...")
          ress <- users.filterNot(u => Settings.whitelist(u.id.id)).traverse { user => 
            for {
              newUser <- bonobo.setRemindedOn(user, now)
              res <- email.sendReminder(newUser)
            } yield (newUser.id -> res)
          }.map(_.toMap)
          _ <- logger.info("aaaand that's a wrap! See you next time.")
        } yield FullRun(ress)
}

