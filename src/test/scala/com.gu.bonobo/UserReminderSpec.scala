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

    val settings = ScheduledSettings(
        Regions.fromName("eu-west-1"),
        DynamoSettings(""),
        DynamoSettings(""),
        "",
        "",
        "",
        EmailSettings(Email("")),
    )

    val userReminder = new UserReminder(settings, emailService, bonoboService, loggingService)
    val userDidNotAnswer = new UserDidNotAnswer(settings, emailService, bonoboService, loggingService)

    "The Reminder service" should "send reminders, duh" in {
        val ((newKeys, _, emailService), sentEmails) = userReminder.run(todayInstant, false).run((keys, users, Set.empty)).value
        val remindedUsers = newKeys.filter(_._2.remindedOn.exists(_ == todayInstant)).map(_._2.userId).toSet
        forAll(remindedUsers) { u => 
            emailService should contain (users(u).email)  
        }
        sentEmails shouldBe a [FullRun]
    }

    "The DidNotAnswer service" should "remove expired keys" in {
        val ((newKeys, _, emailService), _) = userDidNotAnswer.run(false).run((keys, users, Set.empty)).value
        val deletedKeys = newKeys.filter(_._2.remindedOn.exists(t => today.minus(Settings.gracePeriod).toInstant.compareTo(t) >= 0))
        deletedKeys.size should be (0)
    }
}