name := "data-management-system-core"
version := "0.2"
scalaVersion := "2.12.15"

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
val UuidVersion: String = "0.4.0"

libraryDependencies ++= Seq(
    "com.47deg" %% "memeid4s" % UuidVersion,
    "com.chuusai" %% "shapeless" % ShapelessVersion,
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
    "org.scalaj" %% "scalaj-http" % ScalajVersion,
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

Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-u", "core/target")
