scalaVersion := "2.12.10"
name := "movesuggester"
organization := "com.github.lsund"
version := "1.0.0"

// Dependencies
libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
libraryDependencies += "org.apache.kafka" %% "kafka" % "2.3.0"
libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0-RC2"

val circeVersion = "0.12.2"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
