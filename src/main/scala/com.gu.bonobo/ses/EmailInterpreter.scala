package com.gu.bonobo
package services

import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.simpleemail.model.{ Destination => SESDestination, Message => SESMessage, _ }
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClientBuilder
import monix.eval.{Callback, Task}
import monix.execution.Cancelable
import monix.java8.eval._
import scala.collection.JavaConverters._
import scala.util.{Success, Failure}

import config._
import model._

final class EmailInterpreter(config: Settings, logger: LoggingService[Task]) extends EmailService[Task] {
  def sendReminder(origin: Email, destination: Destination, keys: Vector[Key]) = 
    sendEmail(origin, destination, config.email.reminderSubject, reminderEmail(destination.to, keys))
  def sendDeleted(origin: Email, destination: Destination, keys: Vector[Key]) = 
    sendEmail(origin, destination, config.email.deletedSubject, deletedEmail(destination.to, keys))

  private def sendEmail(origin: Email, destination: Destination, subject: String, content: String) = Task.create { (_, callback: Callback[EmailResult]) =>
    val request = new SendEmailRequest()
      .withSource(origin.email)
      .withDestination(new SESDestination().withToAddresses(destination.to.email).withCcAddresses(destination.cc.map(_.email): _*))
      .withMessage(new SESMessage().withSubject(new Content(subject)).withBody(new Body().withHtml(new Content(content))))

    emailClient.sendEmailAsync(request, new AsyncHandler[SendEmailRequest, SendEmailResult]() {
      override def onSuccess(request: SendEmailRequest, response: SendEmailResult) = {
        logger.info(s"Reminder email sent to ${destination.to}")
        callback(Success(EmailResult(response.getMessageId)))
      }

      override def onError(error: Exception) = {
        logger.warn(s"Could not send email to ${destination.to}: ${error.getMessage}\n${error.getStackTrace}")
        callback(Failure(error))
      }
    })

    Cancelable.empty
  }

  private val emailClient = AmazonSimpleEmailServiceAsyncClientBuilder.standard()
    .withCredentials(config.credentialsProvider)
    .withRegion(config.region)
    .build()

  private def reminderEmail(email: Email, keys: Vector[Key]) = html.reminder(email, keys).toString
  private def deletedEmail(email: Email, keys: Vector[Key]) = html.deleted(email, keys).toString
}