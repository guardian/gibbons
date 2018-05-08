package com.gu.gibbons.ses

import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.simpleemail.model.{ Destination => SESDestination, Message => SESMessage, _ }
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailServiceAsyncClientBuilder, AmazonSimpleEmailServiceAsync}
import monix.eval.{Callback, Task}
import monix.execution.Cancelable
import monix.java8.eval._
import scala.collection.JavaConverters._
import scala.util.{Success, Failure}

import com.gu.gibbons.config._
import com.gu.gibbons.model._
import com.gu.gibbons.services._

final class EmailInterpreter(settings: ScheduledSettings, logger: LoggingService[Task], emailClient: AmazonSimpleEmailServiceAsync) extends EmailService[Task] {
  def sendReminder(destination: Destination, keys: Vector[Key]) = 
    sendEmail(destination, EmailSettings.reminderSubject, reminderEmail(destination.to, keys))
  def sendDeleted(destination: Destination, keys: Vector[Key]) = 
    sendEmail(destination, EmailSettings.deletedSubject, deletedEmail(destination.to, keys))

  private def sendEmail(destination: Destination, subject: String, content: String) = Task.create { (_, callback: Callback[EmailResult]) =>
    val request = new SendEmailRequest()
      .withSource(settings.email.origin.email)
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

  private val gen = new UrlGenerator(settings)

  private def reminderEmail(email: Email, keys: Vector[Key]) = html.reminder(email, keys, gen).toString
  private def deletedEmail(email: Email, keys: Vector[Key]) = html.deleted(email, keys, gen).toString
}

object EmailInterpreter {
  def apply(settings: ScheduledSettings, logger: LoggingService[Task]): Task[EmailInterpreter] = Task.evalOnce {
    val emailClient = AmazonSimpleEmailServiceAsyncClientBuilder.standard()
      .withRegion(settings.region)
      .build()
    new EmailInterpreter(settings, logger, emailClient)
  }
}
