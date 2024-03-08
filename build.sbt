import Dependencies._
import sbtassembly.Log4j2MergeStrategy

lazy val root = (project in file("."))
  .settings(
    name := "gibbons",
    inThisBuild(
      List(
        organization := "com.gu",
        scalaVersion := "2.12.18",
        scalacOptions ++= Seq(
          "-Ypartial-unification",
          "-language:higherKinds",
          "-feature"
        )
      )
    ),
    assembly / assemblyMergeStrategy  := {
      case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.first
      case PathList(ps @ _*) if ps.last == "Log4j2Plugins.dat" => Log4j2MergeStrategy.plugincache
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    assembly / assemblyJarName := "gibbons.jar",
    Test / logBuffered          := false,
    libraryDependencies ++= Seq(
      cats,
      scanamo,
      okhttp,
      bouncyCastle,
      lambdaLog4J,
      log4jCore,
      log4jApi,
      scalaTest % Test
    ) ++ monix ++ circe ++ aws,
    dependencyOverrides ++= Seq(
       "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.2",
       "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.14.2",
    )
  )
  .enablePlugins(SbtTwirl)

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", sys.env.getOrElse("SBT_JUNIT_OUTPUT", "junit"))