package com.gu.gibbons.services.interpreters

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

final class EmailInterpreter(settings: Settings, logger: LoggingService[Task], emailClient: AmazonSimpleEmailServiceAsync) extends EmailService[Task] {
  def sendReminder(user: User) = 
    sendEmail(user, Settings.reminderSubject, reminderEmail(user))
  def sendDeleted(user: User) = 
    sendEmail(user, Settings.deletedSubject, deletedEmail(user))

  private def sendEmail(user: User, subject: String, content: String) = Task.create { (_, callback: Callback[EmailResult]) =>
    val request = new SendEmailRequest()
      .withSource(settings.email.origin.email)
      .withDestination(new SESDestination().withToAddresses(user.email.email))
      .withMessage(new SESMessage().withSubject(new Content(subject)).withBody(new Body().withHtml(new Content(content))))

    emailClient.sendEmailAsync(request, new AsyncHandler[SendEmailRequest, SendEmailResult]() {
      override def onSuccess(request: SendEmailRequest, response: SendEmailResult) = {
        logger.info(s"Reminder email sent to ${user.name} <${user.email}>")
        callback(Success(EmailResult(response.getMessageId)))
      }

      override def onError(error: Exception) = {
        logger.warn(s"Could not send email to ${user.name} <${user.email}>: ${error.getMessage}\n${error.getStackTrace}")
        callback(Failure(error))
      }
    })

    Cancelable.empty
  }

  private val gen = new UrlGenerator(settings)

  private def reminderEmail(user: User) = html.reminder(user, gen).toString
  private def deletedEmail(user: User) = html.deleted(user).toString
}

object EmailInterpreter {
  def apply(settings: Settings, logger: LoggingService[Task]): Task[EmailInterpreter] = Task.evalOnce {
    val emailClient = AmazonSimpleEmailServiceAsyncClientBuilder.standard()
      .withRegion(settings.region)
      .build()
    new EmailInterpreter(settings, logger, emailClient)
  }
}
