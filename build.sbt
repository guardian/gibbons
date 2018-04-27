import Dependencies._

lazy val root = (project in file(".")).
  settings(
    name := "bonobo-gdpr",
    inThisBuild(List(
      organization  := "com.example",
      scalaVersion  := "2.12.5",
      scalacOptions ++= Seq(
        "-Ypartial-unification",
        "-language:higherKinds",
        "-feature"
      )
    )),
    logBuffered in Test := false,
    libraryDependencies ++= Seq(
      cats,
      scanamo,
      lambdaLog4J,
      log4jCore,
      log4jApi,
      scalaTest % Test
    ) ++ monix
  )
