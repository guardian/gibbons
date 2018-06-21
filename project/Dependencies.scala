import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest"            %% "scalatest"             % "3.0.5"
  lazy val cats = "org.typelevel"                 %% "cats-core"             % "1.1.0"
  lazy val lambdaLog4J = "com.amazonaws"          % "aws-lambda-java-log4j2" % "1.0.0"
  lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core"             % "2.8.2"
  lazy val log4jApi = "org.apache.logging.log4j"  % "log4j-api"              % "2.8.2"
  lazy val scanamo = "com.gu"                     %% "scanamo"               % "1.0.0-SNAPSHOT"
  lazy val okhttp = "com.squareup.okhttp3"        % "okhttp"                 % "3.10.0"
  lazy val bouncyCastle = "org.bouncycastle"      % "bcpkix-jdk15on"         % "1.59"

  val awsVersion = "1.11.319"
  lazy val aws = Seq(
    "aws-java-sdk-ses",
    "aws-java-sdk-dynamodb"
  ).map("com.amazonaws" % _ % awsVersion)

  val monixVersion = "3.0.0-RC1"
  lazy val monix = Seq(
    "monix-eval",
    "monix-java"
  ).map("io.monix" %% _ % monixVersion)

  val circeVersion = "0.9.3"
  lazy val circe = Seq(
    "circe-core",
    "circe-generic",
    "circe-parser"
  ).map("io.circe" %% _ % circeVersion)
}
