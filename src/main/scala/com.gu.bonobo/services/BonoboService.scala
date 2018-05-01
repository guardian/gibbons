package com.gu.bonobo
package services

// ------------------------------------------------------------------------
import java.time.Instant
import java.time.temporal.TemporalAmount
import model._
// ------------------------------------------------------------------------

/** The algebra for interacting with the Bonobo database */
trait BonoboService[F[_]] {
    /** Get all the keys which have created, or which
      * have been extended, `period` ago
      *
      * @param period The amount of time during above which a key is
      *               potentially expired
      */
    def getKeys(period: TemporalAmount): F[Vector[Key]]

    /** Get a key by ID
      *
      * @param keyId The key ID
      */
    def getKey(keyId: KeyId): F[Option[Key]]

    /** Gets the number of keys belonging to a user
      *
      * @param userId The user's ID
      */
    def getKeyCountFor(userId: UserId): F[Int]

    /** Get all the keys that are potentially expired but have not
      * been either confirmed or infirmed during an acceptable period
      *
      * @param period The amount of time above which a key can
      *               be deleted
      */
    def getInactiveKeys(period: TemporalAmount): F[Vector[Key]]

    /** Deletes a key
      *
      * @param key The key
      */
    def deleteKey(key: Key): F[Unit]

    /** Extend the key for another 30 months
      *
      * @param key The key
      */
    def setExtendedOn(key: Key, when: Instant): F[Unit]

    /** Start the clock for a 14 days grace period. Users
      * have 14 days to take appropriate action for their
      * keys after receiving the email. If they don't their
      * keys will be automatically deleted.
      *
      * @param key The key
      */
    def setRemindedOn(key: Key, when: Instant): F[Unit]

    /** Gets user by ID
      *
      * @param userId The user's IDs
      */
    def getUser(userId: UserId): F[Option[User]]

    /** Deletes user by ID
      *
      * @param userId The user's IDs
      */
    def deleteUser(userId: UserId): F[Unit]
}
