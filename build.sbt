import NativePackagerKeys._

packageArchetype.java_application

name := """tepkin"""

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions := Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import" // 2.11 only
)
  
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-http" % "6.24.0",
  "net.fehmicansaglam" %% "tepkin" % "0.2-SNAPSHOT"
)
