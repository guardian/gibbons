package com.gu.gibbons

// ------------------------------------------------------------------------
import cats.Monad
import config.Settings
import java.time.Instant
import config._
import model._
import services._
// ------------------------------------------------------------------------

/** Act on a user saying they are still using a CAPI key
  *
  * @param bonobo The bonobo service interpreter
  * @param logger The logging service interpreter
  */
class UserSaidYes[F[_] : Monad](settings: Settings, bonobo: BonoboService[F], logger: LoggingService[F]) {
  import cats.syntax.functor._
  import cats.syntax.flatMap._

  /** Extend the key
    *
    * @param keyId the ID of the key to be extended
    */
  def run(keyId: KeyId): F[BonoboResult] = for {
    _ <- logger.info(s"We are extending key $keyId for another ${settings.inactivityPeriod}")
    key <- bonobo.getKey(keyId)
    res <- key match {
      case None => 
        logger.warn(s"Woops, it appears key $keyId does not exist anymore") >> Monad[F].pure(KeyNotFound(keyId))
      case Some(key) => for {
        _ <- bonobo.setExtendedOn(key, Instant.now)
        _ <- logger.info(s"Voila Monsieur!")
      } yield Success
    }
  } yield res
}