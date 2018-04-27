package com.gu.bonobo
package services

import model._

trait EmailService[F[_]] {
    def sendReminder(origin: Email, destination: Destination, keys: Vector[Key]): F[EmailResult]
    def sendDeleted(origin: Email, destination: Destination, keys: Vector[Key]): F[EmailResult]
}