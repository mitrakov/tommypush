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
  "io.burt" % "jmespath-jackson" % "0.5.1",             // jq for Jackson Json library
)

assembly / assemblyMergeStrategy := {
  case "application.conf"       => MergeStrategy.concat
  case "reference.conf"         => MergeStrategy.concat
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case "META-INF/versions/9/module-info.class" => MergeStrategy.last
  case "module-info.class"      => MergeStrategy.first                   // logback-classic
  case x: String                => MergeStrategy.defaultMergeStrategy(x)
}
assembly / assemblyJarName := "tommypush.jar"
assembly / test := {}
assembly / logLevel := util.Level.Debug
