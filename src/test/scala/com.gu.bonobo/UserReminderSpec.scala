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

    val settings = Settings(
        Regions.fromName("eu-west-1"),
        EmailSettings("", "", "", Email("")),
        DynamoSettings(""),
        DynamoSettings(""),
        ""
    )

    val userReminder = new UserReminder(settings, emailService, bonoboService, loggingService)
    val userDidNotAnswer = new UserDidNotAnswer(settings, emailService, bonoboService, loggingService)
    val userSaidYes = new UserSaidYes(settings, bonoboService, loggingService)
    val userSaidNo = new UserSaidNo(settings, bonoboService, loggingService)

    "The Reminder service" should "send reminders, duh" in {
        val ((newKeys, _, emailService), sentEmails) = userReminder.run(todayInstant).run((keys, users, Set.empty)).value
        val remindedUsers = newKeys.filter(_._2.remindedOn.exists(_ == todayInstant)).map(_._2.userId).toSet
        assert(remindedUsers.forall(u => emailService.contains(users(u).email)))
        assert(sentEmails.length == emailService.size)
    }

    "The SaidNo service" should "delete the key" in {
        val sampleKey = keys.keys.head
        val (repos, res) = userSaidNo.run(sampleKey).run((keys, users, Set.empty)).value
        assert(!repos._1.contains(sampleKey))
        assert(res == Success)
    }

    "The SaidNo service" should "warn if the key does not exist" in {
        val sampleKey = KeyId("blablabla")
        val (repos, res) = userSaidNo.run(sampleKey).run((keys, users, Set.empty)).value
        assert(repos._1 == keys)
        assert(res == KeyNotFound(sampleKey))
    }

    "The SaidYes service" should "extend the key lifetime" in {
        val sampleKey = keys.keys.head
        val (repos, res) = userSaidYes.run(sampleKey).run((keys, users, Set.empty)).value
        assert(!repos._1.get(sampleKey).exists(_.remindedOn == todayInstant))
        assert(res == Success)
    }

    "The SaidYes service" should "warn if the key does not exist" in {
        val sampleKey = KeyId("blablabla")
        val (repos, res) = userSaidYes.run(sampleKey).run((keys, users, Set.empty)).value
        assert(repos._1 == keys)
        assert(res == KeyNotFound(sampleKey))
    }

    "The DidNotAnswer service" should "remove expired keys" in {
        val ((newKeys, _, emailService), _) = userDidNotAnswer.run.run((keys, users, Set.empty)).value
        assert(newKeys.filter(_._2.remindedOn.exists(t => today.minus(Settings.gracePeriod).toInstant.compareTo(t) >= 0)).size == 0)
    }
}