package com.gu.bonobo

// ------------------------------------------------------------------------
import cats.Monad
import config.Settings
import model._
import services._
// ------------------------------------------------------------------------

/** Act on a user saying they're not using a CAPI key anymore
  *
  * @param bonobo The bonobo service interpreter
  * @param logger The logging service interpreter
  */
class UserSaidNo[F[_] : Monad](bonobo: BonoboService[F], logger: LoggingService[F]) {
  import cats.syntax.functor._
  import cats.syntax.flatMap._

  /** Delete the key
    *
    * @param keyId the ID of the key to be deleted
    */
  def run(keyId: KeyId): F[BonoboResult] = for {
    _ <- logger.info(s"User is no longer using the API, let's get rid of key $keyId")
    key <- bonobo.getKey(keyId)
    res <- key match {
      case None => 
        logger.warn(s"Woops, it appears key $keyId does not exist anymore") >> Monad[F].pure(KeyNotFound(keyId))
      case Some(key) => for {
        _ <- bonobo.deleteKey(key)
        _ <- logger.info(s"Boom! Done.")
        _ <- deleterUserIfNeeded(key.userId)
      } yield Success
    }
  } yield res

  private def deleterUserIfNeeded(userId: UserId) = for {
    _ <- logger.info(s"Checking that user $userId still has keys")
    n <- bonobo.getKeyCountFor(userId)
    _ <- if( n == 0 ) 
      for {
        _ <- logger.info("None! This guy is toast")
        _ <- bonobo.deleteUser(userId)
        _ <- logger.info("Zap! He's gone forever.")
      } yield ()
    else Monad[F].pure(())
  } yield ()
}