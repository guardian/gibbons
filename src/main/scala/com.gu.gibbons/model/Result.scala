package com.gu.gibbons
package model

import io.circe.Encoder
import io.circe.generic.semiauto._

sealed trait Result
case class DryRun(keys: Map[UserId, Vector[Key]]) extends Result
case class FullRun(result: Map[UserId, (EmailResult, Vector[Key])]) extends Result
