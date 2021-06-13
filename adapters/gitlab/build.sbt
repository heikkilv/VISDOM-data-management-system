scalaVersion := "2.12.13"
name := "GitLabAdapter"
version := "0.2"

val mongoConnectorVersion: String = "3.0.1"
val sparkVersion: String = "3.1.1"
val loggerVersion: String = "1.7.30"
val SprayJsonVersion: String = "1.3.6"

libraryDependencies ++= Seq(
    "io.spray" %%  "spray-json" % SprayJsonVersion,
    "org.mongodb.spark" %% "mongo-spark-connector" % mongoConnectorVersion,
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
    "org.slf4j" % "slf4j-api" % loggerVersion,
    "org.slf4j" % "slf4j-simple" % loggerVersion
)

ThisBuild / scapegoatVersion := "1.4.8"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.7"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % "test"
libraryDependencies += "org.scalatest" %% "scalatest-funsuite" % "3.2.7" % "test"

wartremoverErrors ++= Warts.unsafe
