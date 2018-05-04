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
    def run(now: Instant, dryRun: Boolean): F[ReminderResult] = 
      if (dryRun) 
        for {
          _ <- logger.info("Yop, who's up for receiving a reminder?")
          keys <- bonobo.getKeys(Settings.inactivityPeriod)
        } yield DryRun(keys.groupBy(_.userId))
      else
        for {
          _ <- logger.info(s"Getting all the keys older than ${Settings.inactivityPeriod}")
          keys <- bonobo.getKeys(Settings.inactivityPeriod)
          _ <- logger.info(s"Found ${keys.length} keys. Let's find out who they belong to...")
          keysByUser = keys.groupBy(_.userId)
          users <- keysByUser.keys.toVector.traverse(id => bonobo.getUser(id)).map(_.flatten)
          _ <- logger.info(s"Found ${users.length} users. Let's send some emails...")
          ress <- users.traverse(user => email.sendReminder(Destination(user.email), keysByUser(user.id)) >>= (res => Monad[F].pure((user.id -> res))))
          ress2 = ress.toMap
          _ <- logger.info(s"Sent all the emailz! Let's make sure we keep track of that...")
          _ <- keys.traverse(key => bonobo.setRemindedOn(key, now))
          _ <- logger.info("aaaand that's a wrap! See you next time.")
        } yield FullRun(keysByUser.map { case (uid, keys) => uid -> (ress2(uid), keys) })
}

sealed trait ReminderResult
case class DryRun(keys: Map[UserId, Vector[Key]]) extends ReminderResult
case class FullRun(result: Map[UserId, (EmailResult, Vector[Key])]) extends ReminderResult