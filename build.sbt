import Dependencies._
import sbtassembly.Log4j2MergeStrategy

lazy val root = (project in file(".")).
  settings(
    name := "gibbons",
    inThisBuild(List(
      organization  := "com.example",
      scalaVersion  := "2.12.5",
      scalacOptions ++= Seq(
        "-Ypartial-unification",
        "-language:higherKinds",
        "-feature"
      )
    )),

    assemblyMergeStrategy in assembly := {
      case PathList(ps @ _*) if ps.last == "Log4j2Plugins.dat" => Log4j2MergeStrategy.plugincache
    },
    logBuffered in Test := false,
    libraryDependencies ++= Seq(
      cats,
      scanamo,
      okhttp,
      lambdaLog4J,
      log4jCore,
      log4jApi,
      scalaTest % Test
    ) ++ monix ++ circe ++ aws
  ).
  enablePlugins(SbtTwirl)