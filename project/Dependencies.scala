import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val cats = "org.typelevel" %% "cats-core" % "1.1.0"
  lazy val lambdaLog4J = "com.amazonaws" % "aws-lambda-java-log4j2" % "1.1.0"
  lazy val log4jCore = "org.apache.logging.log4j" % "log4j-core" % "2.11.0"
  lazy val log4jApi = "org.apache.logging.log4j" % "log4j-api" % "2.11.0"
  lazy val scanamo = "com.gu" %% "scanamo" % "1.0.0-M6"
  lazy val okhttp = "com.squareup.okhttp3" % "okhttp" % "3.10.0"
  
  val monixVersion = "2.3.3"
  lazy val monix = Seq(
    "io.monix" %% "monix-eval",
    "io.monix" %% "monix-cats"
  ).map(_ % monixVersion)


  val circeVersion = "0.9.3"
  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)
}
