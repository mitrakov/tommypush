name := "serverpush"
organization := "com.mitrakov.self"
version := "1.0.0"
scalaVersion := "2.13.7"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "ch.qos.logback" % "logback-classic" % "1.2.5" % Runtime,
  "com.google.firebase" % "firebase-admin" % "9.1.1"
)
