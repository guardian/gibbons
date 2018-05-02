package com.gu.gibbons
package services

import model._

trait EmailService[F[_]] {
    def sendReminder(destination: Destination, keys: Vector[Key]): F[EmailResult]
    def sendDeleted(destination: Destination, keys: Vector[Key]): F[EmailResult]
}