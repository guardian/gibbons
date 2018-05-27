package com.gu.gibbons.config

import org.scalatest._

class SettingsSpec extends FlatSpec with Matchers with Inspectors {
  private val validEnv = Map(
    "AWS_REGION"         -> "eu-west-1",
    "BONOBO_USERS_TABLE" -> "How",
    "SALT"               -> "Are",
    "BONOBO_URL"         -> "You",
    "EMAIL_ORIGIN"       -> "Doing"
  )

  private val emptyEnv: Map[String, String] = Map.empty

  "Parsing settings" should "produce a valid settings instance" in {
    val result = Settings.parseEnv(validEnv)
    
    result.isValid should be (true)
    result.fold(
      _ => fail("Hmm, Houston, we should have a Settings here"),
      r => r shouldBe a [Settings]
    )
  }

  it should "find all missing keys" in {
    val result = Settings.parseEnv(emptyEnv)

    result.isValid should be (false)
    result.fold(
      es => es.length should be (5),
      _ => fail("Won't happen")
    )
  }
}
