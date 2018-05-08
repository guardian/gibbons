package com.gu.gibbons
package services

import model._

trait EmailService[F[_]] {
    def sendReminder(user: User, keys: Vector[Key]): F[EmailResult]
    def sendDeleted(user: User, keys: Vector[Key]): F[EmailResult]
}