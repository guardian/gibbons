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
        Email("")
    )

    val userReminder = new UserReminder(settings, emailService, bonoboService, loggingService)
    val userDidNotAnswer = new UserDidNotAnswer(settings, emailService, bonoboService, loggingService)

    "The Reminder service" should "send reminders, duh" in {
        val ((newUsers, emailService), sentEmails) = userReminder.run(todayInstant, false).run((users, Set.empty)).value
        val remindedUsers = newUsers.filter(_._2.remindedAt.exists(_ == todayInstant)).map(_._2.id).toSet
        forAll(remindedUsers) { u => 
            emailService should contain (users(u).email)  
        }
        sentEmails shouldBe a [FullRun]
    }

    "The DidNotAnswer service" should "remove expired keys" in {
        val ((newUsers, emailService), _) = userDidNotAnswer.run(false).run((users, Set.empty)).value
        val deletedKeys = newUsers.filter(_._2.remindedAt.exists(t => today.minus(Settings.gracePeriod).toInstant.compareTo(t) >= 0))
        
        deletedKeys.size shouldBe 0
    }
}