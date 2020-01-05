import sbt._

object Dependencies {
  lazy val cmdLineOpts =  "com.github.scopt" %% "scopt" % "3.6.0"
  lazy val typesafeConfig = "com.typesafe" % "config" % "1.3.4"
  lazy val kafkaClient = "org.apache.kafka" % "kafka-clients" % "2.3.0" excludeAll ExclusionRule (organization = "org.slf4j")
  lazy val kstreams = "org.apache.kafka" % "kafka-streams" % "2.3.0" excludeAll ExclusionRule (organization = "org.slf4j")
  lazy val kstreamsScala = "org.apache.kafka" %% "kafka-streams-scala" % "2.3.0" excludeAll ExclusionRule(organization ="com.fasterxml.jackson.core")
  lazy val jacksonScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.9"
  lazy val joda = "joda-time" % "joda-time" % "2.10.3"
  lazy val awsSDK = "com.amazonaws" % "aws-java-sdk" % "1.11.598" excludeAll(
    ExclusionRule(organization="com.fasterxml.jackson.core"), ExclusionRule(organization = "com.fasterxml.jackson.dataformat"),
    ExclusionRule(organization="joda-time"))
  lazy val metrics = "nl.grons" %% "metrics4-scala" % "4.0.8" excludeAll ExclusionRule (organization = "org.slf4j")
  lazy val metricsHeader = "nl.grons" %% "metrics4-scala-hdr" % "4.0.8" excludeAll ExclusionRule (organization = "org.slf4j")
  lazy val dropWizardMetrics = "io.dropwizard.metrics" % "metrics-jmx" % "4.1.0" excludeAll ExclusionRule (organization = "org.slf4j")
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

  //TEST
  lazy val kstreamsTest = "org.apache.kafka" % "kafka-streams-test-utils" % "2.3.0" 
  lazy val scalaMock = "org.scalamock" %% "scalamock" % "4.1.0" 
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

}
