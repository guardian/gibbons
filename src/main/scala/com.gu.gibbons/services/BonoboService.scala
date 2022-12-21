package com.gu.gibbons
package services

// ------------------------------------------------------------------------
import java.time.Instant
import model._
// ------------------------------------------------------------------------

/** The algebra for interacting with the Bonobo database */
trait BonoboService[F[_]] {

  /** Get all the developer tier keys which have been created, or which
   * have been last extended, before certain date
   *
   * @param createdBefore  The date before which a key is
   *               considered potentially inactive
   */
  def getPotentiallyInactiveDeveloperKeys(createdBefore: Instant): F[Vector[Key]]

  /** Get all the keys where the owner has ignored the reminder email to extend their key
   *
   * @param remindedBefore The date before which a user can  be deleted
   */
  def getIgnoredReminderKeys(remindedBefore: Instant): F[Vector[Key]]

  /** Get all the users who failed to verify their email address within verificationGracePeriod
   *
   * @param verificationSentBefore The date before which a user can  be deleted
   */
  def getUnverifiedUsers(verificationSentBefore: Instant): F[Vector[User]]

  /** Get user who owns certain key
   *
   * @param key The key whose owners we want to notify
   */
  def getKeyOwner(key: Key): F[User]

  /** Get keys owned by a certain user
   *
   * @param user The user whose keys we want to delete
   */
  def getKeysByOwner(user: User): F[Vector[Key]]



  /** Start the clock for reminder grace period (default set to 14 days). Users
   * have 14 days to take appropriate action for their
   * keys after receiving the email. If they don't their
   * key will be deleted.
   *
   * @param key The Key to be either extended or deleted
   */
  def setRemindedAt(key: Key, when: Long): F[Key]

  /** Deletes a key
   *
   * @param key The key to be deleted
   */
  def deleteKey(key: Key): F[Unit]

  /** Deletes a user and all their keys
   *
   * @param user The User to be deleted
   */
  def deleteUnverifiedUserAndKeys(user: User): F[Unit]

}
