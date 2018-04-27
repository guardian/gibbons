package com.gu.bonobo

import org.scalatest._
import fixtures._
import services._
import model._

class IntegrationTests extends FlatSpec {
    val emailService = new EmailServiceInterpreter {}
    val bonoboService = new BonoboServiceInterpreter {} 
    val loggingService = new LoggingServiceInterpreter {}

    val userReminder = new UserReminder(emailService, bonoboService, loggingService)
    val userDidNotAnswer = new UserDidNotAnswer(emailService, bonoboService, loggingService)
    val userSaidYes = new UserSaidYes(bonoboService, loggingService)
    val userSaidNo = new UserSaidNo(bonoboService, loggingService)

    "The Reminder service" should "send reminders, duh" in {
        val (repos, sentEmails) = userReminder.run.run((keys, users)).value
        val sentKeys = repos._1.filter(_._2.remindedOn.exists(_ == today)).keys
        assert(sentKeys.forall { k => sentEmails.exists(_.msgId.contains(k)) })
    }

    "The SaidNo service" should "delete the key" in {
        val sampleKey = keys.keys.head
        val (repos, res) = userSaidNo.run(sampleKey).run((keys, users)).value
        assert(!repos._1.contains(sampleKey))
        assert(res == Success)
    }

    "The SaidNo service" should "warn if the key does not exist" in {
        val sampleKey = KeyId("blablabla")
        val (repos, res) = userSaidNo.run(sampleKey).run((keys, users)).value
        assert(repos._1 == keys)
        assert(res == KeyNotFound(sampleKey))
    }

    "The SaidYes service" should "extend the key lifetime" in {
        val sampleKey = keys.keys.head
        val (repos, res) = userSaidYes.run(sampleKey).run((keys, users)).value
        assert(!repos._1.get(sampleKey).exists(_.remindedOn == today.toInstant))
        assert(res == Success)
    }

    "The SaidYes service" should "warn if the key does not exist" in {
        val sampleKey = KeyId("blablabla")
        val (repos, res) = userSaidYes.run(sampleKey).run((keys, users)).value
        assert(repos._1 == keys)
        assert(res == KeyNotFound(sampleKey))
    }
}