package com.gu.gibbons

import com.amazonaws.regions.Regions
import org.scalatest._

import fixtures._
import services._
import model._
import config._

class IntegrationTests extends FlatSpec with Matchers with Inspectors {
    val emailService = new EmailServiceInterpreter {}
    val bonoboService = new BonoboServiceInterpreter {} 
    val loggingService = new LoggingServiceInterpreter {}

    val settings = Settings(
        Regions.fromName("eu-west-1"),
        "",
        "",
        "",
        "",
        Email("")
    )

    val userReminder = new UserReminder(settings, emailService, bonoboService, loggingService)
    val userDidNotAnswer = new UserDidNotAnswer(settings, emailService, bonoboService, loggingService)

    "The Reminder service" should "send reminders, duh" in {
        val ((newUsers, emailService, _), sentEmails) = userReminder.run(today, false).run((users, Set.empty, keys)).value
        val remindedUsers = newUsers.filter(_._2.remindedAt.exists(_ == todayInstant.toEpochMilli)).map(_._2.id).toSet
        forAll(remindedUsers) { u => 
            emailService should contain (users(u).email)  
        }
        sentEmails should not be empty
    }

    "The DidNotAnswer service" should "remove expired keys" in {
        val ((newUsers, emailService, _), _) = userDidNotAnswer.run(false).run((users, Set.empty, keys)).value
        val deletedKeys = newUsers.filter(_._2.remindedAt.exists(t => today.minus(Settings.gracePeriod).toInstant.toEpochMilli >= t))
        
        deletedKeys.size shouldBe 0
    }
}