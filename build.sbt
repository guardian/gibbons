import Dependencies._
import sbtassembly.Log4j2MergeStrategy

lazy val root = (project in file(".")).
  settings(
    name := "gibbons",
    inThisBuild(List(
      organization  := "com.gu",
      scalaVersion  := "2.12.5",
      scalacOptions ++= Seq(
        "-Ypartial-unification",
        "-language:higherKinds",
        "-feature"
      )
    )),

    assemblyMergeStrategy in assembly := {
      case "Log4j2Plugins.dat" => Log4j2MergeStrategy.plugincache
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    assemblyJarName in assembly := "gibbons.jar",

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