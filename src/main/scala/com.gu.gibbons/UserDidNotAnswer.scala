package com.gu.gibbons

// ------------------------------------------------------------------------
import cats.Monad
import config.Settings
import model.Destination
import services._
// ------------------------------------------------------------------------

/** Deletes keys for those users who have been sent a reminder but failed to 
  * answer with xx days
  *
  * @param email The email service interpreter
  * @param bonobo The bonobo service interpreter
  * @param logger The logging service interpreter
  */
class UserDidNotAnswer[F[_] : Monad](settings: Settings, email: EmailService[F], bonobo: BonoboService[F], logger: LoggingService[F]) {
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
    def run: F[Unit] = for {
        _ <- logger.info("Getting all the keys which have not been extended since ${Settings.gracePeriod}")
        keys <- bonobo.getInactiveKeys(Settings.gracePeriod)
        _ <- logger.info(s"Found ${keys.length} keys. Let's find out who the belong to...")
        keysByUser = keys.groupBy(_.userId)
        users <- keysByUser.keys.toVector.traverse(id => bonobo.getUser(id)).map(_.flatten)
        _ <- logger.info(s"Found ${users.length} users. Let's delete these keys...")
        _ <- keys.traverse(key => bonobo.deleteKey(key))
        _ <- logger.info("Swell! Now we can send a last email to those poor souls...")
        _ <- users.traverse(user => email.sendDeleted(settings.email.origin, Destination(user.email), keysByUser(user.id)))
        _ <- logger.info("That's a wrap! See ya.")
    } yield ()
}