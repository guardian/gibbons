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

}
