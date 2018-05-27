package com.gu.gibbons
package lambdas

import cats.data.{ Validated, ValidatedNel }
import com.amazonaws.services.lambda.runtime.Context; 
import java.time.Instant
import io.circe.Json
import io.circe.parser.decode
import io.circe.syntax._
import java.io.{InputStream, OutputStream}
import monix.execution.Scheduler.Implicits.global
import monix.eval.Task
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.io.Source

import config._
import model.JsonFormats
import services.interpreters._

abstract class GenericLambda(implicit ec: ExecutionContext) {
  import cats.implicits._

  def handleRequest(is: InputStream, os: OutputStream, context: Context) = {
    ( Settings.fromEnvironment
    , readArgs(is)
    ).mapN(go).map { program =>
      Await.result(program.runAsync, Duration(context.getRemainingTimeInMillis, MILLISECONDS))
    }.fold(
      error => os.write(s"Something went horribly wrong: $error".getBytes), 
      result => os.write(result.toString.getBytes)
    )

    is.close()
    os.close()
  }

  def go(settings: Settings, dryRun: Boolean): Task[Json]
 
  private def readArgs(is: InputStream): ValidatedNel[String, Boolean] = Validated.fromEither {
    val input = Source.fromInputStream(is).mkString
    decode[Boolean](input).leftMap(_.toString)
  }.toValidatedNel
}