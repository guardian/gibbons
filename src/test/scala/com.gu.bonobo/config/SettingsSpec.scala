package com.gu.gibbons.config

import org.scalatest._

class SettingsSpec extends FlatSpec with Matchers with Inspectors {
  private val validEnv = Map(
    "AWS_REGION"         -> "eu-west-1",
    "BONOBO_USERS_TABLE" -> "How",
    "BONOBO_KEYS_TABLE"  -> "Well",
    "SALT"               -> "Are",
    "BONOBO_URL"         -> "You",
    "EMAIL_ORIGIN"       -> "Doing"
  )

  private val invalidRegion = Map(
    "AWS_REGION"         -> "Hello",
    "BONOBO_USERS_TABLE" -> "How",
    "BONOBO_KEYS_TABLE"  -> "Well",
    "SALT"               -> "Are",
    "BONOBO_URL"         -> "You",
    "EMAIL_ORIGIN"       -> "Doing"
  )

  private val emptyEnv: Map[String, String] = Map.empty

  "Parsing settings" should "produce a valid settings instance" in {
    val result = Settings.parseEnv(validEnv)
    
    result.isValid shouldBe true
    result.fold(
      _ => fail("Hmm, Houston, we should have a Settings here"),
      r => r shouldBe a [Settings]
    )
  }

  it should "catch invalid region" in {
    val result = Settings.parseEnv(invalidRegion)

    result.isValid shouldBe false
    result.fold(
      es => es.length shouldBe 1,
      _ => fail("Won't happen")
    )
  }

  it should "find all missing keys" in {
    val result = Settings.parseEnv(emptyEnv)

    result.isValid shouldBe false
    result.fold(
      es => es.length shouldBe validEnv.size,
      _ => fail("Won't happen")
    )
  }
}
