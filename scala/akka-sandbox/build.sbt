name := """akka-sandbox"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.0",
  "junit" % "junit" % "4.12",
  "org.scalatest" %% "scalatest" % "2.1.3"
)
