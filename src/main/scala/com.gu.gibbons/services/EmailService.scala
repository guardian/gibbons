package com.gu.gibbons
package services

import model._

trait EmailService[F[_]] {
    def sendReminder(user: User): F[EmailResult]
    def sendDeleted(user: User): F[EmailResult]
}