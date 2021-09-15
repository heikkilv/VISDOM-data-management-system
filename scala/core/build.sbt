name := "data-management-system-core"
version := "0.2"

val AkkaVersion = "2.6.14"
val AkkaHttpVersion = "10.2.4"
val JavaWsRestApiVersion: String = "2.1.1"
val LoggerVersion: String = "1.8.0-beta4"
val MongoConnectorVersion: String = "3.0.1"
val MongoDriverVersion: String = "4.0.5"
val SparkVersion: String = "3.1.1"
val ScalajVersion: String = "2.4.2"
val ScalaTestVersion: String = "3.2.7"
val ScapeGoatVersion: String = "1.4.8"
val SprayJsonVersion: String = "1.3.6"
val SwaggerAkkaVersion: String = "2.4.2"

libraryDependencies ++= Seq(
    "com.github.swagger-akka-http" %% "swagger-akka-http" % SwaggerAkkaVersion,
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    "io.spray" %%  "spray-json" % SprayJsonVersion,
    "javax.ws.rs" % "javax.ws.rs-api" % JavaWsRestApiVersion,
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
