scalaVersion := "2.12.13"
name := "GitLabAdapter"
version := "0.2"

val AkkaVersion = "2.6.14"
val AkkaHttpVersion = "10.2.4"
val MongoConnectorVersion: String = "3.0.1"
val MongoDriverVersion: String = "4.0.5"
val SparkVersion: String = "3.1.1"
val ScalaTestVersion: String = "3.2.7"
val ScapeGoatVersion: String = "1.4.8"
val SwaggerAkkaVersion: String = "2.4.2"

libraryDependencies ++= Seq(
    "com.github.swagger-akka-http" %% "swagger-akka-http" % SwaggerAkkaVersion,
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    "org.apache.spark" %% "spark-core" % SparkVersion % "provided",
    "org.apache.spark" %% "spark-sql" % SparkVersion % "provided",
    "org.mongodb.spark" %% "mongo-spark-connector" % MongoConnectorVersion,
    "org.mongodb.scala" %% "mongo-scala-driver" % MongoDriverVersion,
    "org.scalactic" %% "scalactic" % ScalaTestVersion,
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
    "org.scalatest" %% "scalatest-funsuite" % ScalaTestVersion % "test"
)

ThisBuild / scapegoatVersion := ScapeGoatVersion

wartremoverErrors ++= Warts.unsafe

scalacOptions ++= Seq("-deprecation", "-feature")

// to get rid of deduplicate errors, from https://stackoverflow.com/a/67937671
ThisBuild / assemblyMergeStrategy  := {
    case PathList("module-info.class") => MergeStrategy.discard
    case name if name.endsWith("/module-info.class") => MergeStrategy.discard
    case name => {
        val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
        oldStrategy(name)
    }
}
