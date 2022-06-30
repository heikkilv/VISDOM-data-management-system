// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

name := "adapter-course"
version := "0.2"
scalaVersion := "2.12.15"

val AdapterMain: String = "visdom.adapters.course.CourseAdapter"

Compile / mainClass := Some(AdapterMain)
assembly / mainClass := Some(AdapterMain)
assembly / assemblyJarName := s"${name.value}-${version.value}.jar"

val AkkaVersion = "2.6.17"
val AkkaHttpVersion = "10.2.7"
val JakartaVersion: String = "3.0.0"
val LoggerVersion: String = "2.0.0-alpha5"
val MongoConnectorVersion: String = "3.0.1"
val MongoDriverVersion: String = "4.1.1"
val SparkVersion: String = "3.1.1"
val ScalajVersion: String = "2.4.2"
val ScalaTestVersion: String = "3.2.10"
val ScapeGoatVersion: String = "1.4.11"
val ShapelessVersion: String = "2.3.7"
val SprayJsonVersion: String = "1.3.6"
val SwaggerAkkaVersion: String = "2.6.0"

libraryDependencies ++= Seq(
    "com.github.swagger-akka-http" %% "swagger-akka-http" % SwaggerAkkaVersion,
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    "io.spray" %%  "spray-json" % SprayJsonVersion,
    "jakarta.ws.rs" % "jakarta.ws.rs-api" % JakartaVersion,
    "org.apache.spark" %% "spark-core" % SparkVersion % "provided",
    "org.apache.spark" %% "spark-sql" % SparkVersion % "provided",
    "org.mongodb.scala" %% "mongo-scala-driver" % MongoDriverVersion,
    "org.mongodb.spark" %% "mongo-spark-connector" % MongoConnectorVersion,
    "org.scalactic" %% "scalactic" % ScalaTestVersion,
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
    "org.scalatest" %% "scalatest-funsuite" % ScalaTestVersion % "test",
    "org.slf4j" % "slf4j-api" % LoggerVersion,
    "org.slf4j" % "slf4j-simple" % LoggerVersion
)

ThisBuild / scapegoatVersion := ScapeGoatVersion

wartremoverErrors ++= Warts.unsafe

scalacOptions ++= Seq("-deprecation", "-feature")

dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang")
dependencyUpdatesFilter -= moduleFilter(organization = "org.apache.spark")

// to get rid of class not found errors with shapeless library
// from https://github.com/pureconfig/pureconfig/issues/337#issue-280450747
assembly / assemblyShadeRules := Seq(
  ShadeRule.rename("shapeless.**" -> "new_shapeless.@1").inAll
)

// to get rid of deduplicate errors, from https://stackoverflow.com/a/67937671
ThisBuild / assemblyMergeStrategy := {
    case PathList("module-info.class") => MergeStrategy.discard
    case name if name.endsWith("/module-info.class") => MergeStrategy.discard
    case name => {
        val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
        oldStrategy(name)
    }
}
