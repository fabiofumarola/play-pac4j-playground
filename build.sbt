name := """play-pac4j-playground"""
organization := "it.gov.daf"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "it.gov.daf.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "it.gov.daf.binders._"
