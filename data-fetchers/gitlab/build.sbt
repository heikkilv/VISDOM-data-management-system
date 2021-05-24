scalaVersion := "2.12.13"
name := "gitlab-data-fetcher"
version := "0.1"

val scalajVersion: String = "2.4.2"
val circeVersion: String = "0.12.3"
val mongoDriverVersion: String = "4.2.3"
val loggerVersion: String = "1.7.30"

libraryDependencies ++= Seq(
    "io.circe" %% "circe-core"    % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser"  % circeVersion,
    "org.mongodb.scala" %% "mongo-scala-driver" % mongoDriverVersion,
    "org.scalaj" %% "scalaj-http" % scalajVersion,
    "org.slf4j" % "slf4j-api" % loggerVersion,
    "org.slf4j" % "slf4j-simple" % loggerVersion
)

ThisBuild / scapegoatVersion := "1.4.8"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.7"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % "test"
libraryDependencies += "org.scalatest" %% "scalatest-funsuite" % "3.2.7" % "test"

wartremoverErrors ++= Warts.unsafe

enablePlugins(JavaAppPackaging)
