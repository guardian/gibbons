package com.gu.gibbons
package services

// ------------------------------------------------------------------------
import java.time.Instant
import model._
// ------------------------------------------------------------------------

/** The algebra for interacting with the Bonobo database */
trait BonoboService[F[_]] {
    /** Get all the users which have been created, or which
      * have been extended, before `jadis`
      *
      * @param jadis  The time before which a user is
      *               potentially expired
      */
    def getUsers(jadis: Instant): F[Vector[User]]

    def isDeveloper(users: Vector[User]): F[Vector[User]]

    /** Get all the users that are potentially expired but have not
      * either confirmed or infirmed during the grace period
      *
      * @param jadis The time before which a user can  be deleted
      */
    def getInactiveUsers(jadis: Instant): F[Vector[User]]

    /** Start the clock for a 14 days grace period. Users
      * have 14 days to take appropriate action for their
      * keys after receiving the email. If they don't their
      * account and keys will be automatically deleted.
      *
      * @param user The user
      */
    def setRemindedOn(user: User, when: Long): F[User]

    /** Deletes a user and all their keys
      *
      * @param user The user
      */
    def deleteUser(user: User): F[Unit]

    def oldEnough(user: User, jadis: Long) =
      !user.remindedAt.isDefined && (
        user.extendedAt.exists(_ <= jadis) || !user.extendedAt.isDefined && user.createdAt <= jadis
      )
}
