resolvers += Resolver.url(
  "idio",
  url("http://dl.bintray.com/idio/sbt-plugins")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")
addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.3.13")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")
addSbtPlugin("org.idio" % "sbt-assembly-log4j2" % "0.1.0")
