import scalariform.formatter.preferences._

organization := "procensus"

name := "challenge"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"

resolvers += Resolver.mavenLocal

val akkaVersion = "2.3.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "2.+" % Test,
  "junit" % "junit" % "4.12" % Test,
  "com.novocode" % "junit-interface" % "0.10" % Test,
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "org.slf4j" % "slf4j-simple" % "1.7.10",
  "io.spray"  %%  "spray-can"     % "1.3.2",
  "io.spray"  %%  "spray-routing" % "1.3.2",
  "io.spray"  %%  "spray-json"    % "1.3.1",
  "io.spray"  %%  "spray-testkit"    % "1.3.2" % Test,
  "com.softwaremill.macwire" %% "macros" % "1.0.5",
  "com.softwaremill.macwire" %% "runtime" % "1.0.5",
  "com.wandoulabs.akka" %% "spray-websocket" % "0.1.4" excludeAll(
    ExclusionRule(organization = "io.spray")
  )
).map(_.withSources())

com.typesafe.sbt.SbtScalariform.scalariformSettings

ScalariformKeys.preferences := PreferencesImporterExporter.loadPreferences(baseDirectory.value / "project" / "formatterPreferences.properties" toString)

Revolver.settings

scalariformSettings

coverageEnabled := false

mainClass in (Compile, run) := Some("mathquiz.MathQuizBoot")

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

fork := true

connectInput in run := true

outputStrategy := Some(StdoutOutput)