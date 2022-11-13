name := "serverpush"
organization := "com.mitrakov.self"
version := "1.0.0"
scalaVersion := "2.13.7"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.4" % Runtime,
  "com.google.firebase" % "firebase-admin" % "9.1.1",   // firebase messaging
  "com.softwaremill.sttp.client3" %% "core" % "3.8.3",  // simple http client
  "io.burt" % "jmespath-jackson" % "0.5.1"              // jq for Jackson Json library
)
