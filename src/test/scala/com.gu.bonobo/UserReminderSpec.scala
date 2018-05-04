package com.gu.gibbons

import com.amazonaws.regions.Regions
import org.scalatest._

import fixtures._
import services._
import model._
import config._

class IntegrationTests extends FlatSpec {
    val emailService = new EmailServiceInterpreter {}
    val bonoboService = new BonoboServiceInterpreter {} 
    val loggingService = new LoggingServiceInterpreter {}

    val settings = ScheduledSettings(
        Regions.fromName("eu-west-1"),
        DynamoSettings(""),
        DynamoSettings(""),
        "",
        "",
        EmailSettings("", "", Email("")),
    )

    val userReminder = new UserReminder(settings, emailService, bonoboService, loggingService)
    val userDidNotAnswer = new UserDidNotAnswer(settings, emailService, bonoboService, loggingService)

    "The Reminder service" should "send reminders, duh" in {
        val ((newKeys, _, emailService), sentEmails) = userReminder.run(todayInstant).run((keys, users, Set.empty)).value
        val remindedUsers = newKeys.filter(_._2.remindedOn.exists(_ == todayInstant)).map(_._2.userId).toSet
        assert(remindedUsers.forall(u => emailService.contains(users(u).email)))
        assert(sentEmails.length == emailService.size)
    }

    "The DidNotAnswer service" should "remove expired keys" in {
        val ((newKeys, _, emailService), _) = userDidNotAnswer.run.run((keys, users, Set.empty)).value
        assert(newKeys.filter(_._2.remindedOn.exists(t => today.minus(Settings.gracePeriod).toInstant.compareTo(t) >= 0)).size == 0)
    }
}