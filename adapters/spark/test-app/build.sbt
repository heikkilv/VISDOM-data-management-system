scalaVersion := "2.12.13"
name := "SimpleTestProject"
version := "0.1"

val sparkVersion: String = "3.1.1"
val loggerVersion: String = "1.7.30"

libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
    "org.slf4j" % "slf4j-api" % loggerVersion,
    "org.slf4j" % "slf4j-simple" % loggerVersion
)
