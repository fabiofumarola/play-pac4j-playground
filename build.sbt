
scalaVersion := "2.12.3"

val playVersion = "2.6.7"
val pac4jVersion = "2.2.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, ParadoxPlugin)
  .settings(
    organization := "it.gov.daf",
    name := """play-pac4j-playground""",
    version := "1.0.0-SNAPSHOT",
    fork in run := true
  )
    .settings(
      paradoxTheme := Some(builtinParadoxTheme("generic"))
    )
  .settings(
    libraryDependencies ++= Seq(
      guice,
      ehcache,
      filters,
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "org.pac4j" % "play-pac4j" % "4.1.1",
      "org.pac4j" % "pac4j-http" % pac4jVersion,
      "com.typesafe.play" %% "play-cache" % playVersion
    )
  )


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "it.gov.daf.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "it.gov.daf.binders._"

