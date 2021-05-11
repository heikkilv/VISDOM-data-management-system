scalaVersion := "2.12.13"
name := "gitlab-data-fetcher"
version := "0.1"

val scalajVersion: String = "2.4.2"
val circeVersion: String = "0.12.3"
ThisBuild / scapegoatVersion := "1.4.8"

libraryDependencies ++= Seq(
    "org.scalaj" %% "scalaj-http" % scalajVersion,
    "io.circe" %% "circe-core"    % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser"  % circeVersion
)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.7"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % "test"
libraryDependencies += "org.scalatest" %% "scalatest-funsuite" % "3.2.7" % "test"

wartremoverErrors ++= Warts.unsafe

enablePlugins(JavaAppPackaging)
