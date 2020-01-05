import Dependencies._

ThisBuild / scalaVersion     := "2.11.12"
ThisBuild / organization     := "com.plume."
ThisBuild / organizationName := "sdata"

//dependencies
lazy val root = (project in file("."))
  .settings(
    name := "kstreams-base",
    libraryDependencies += cmdLineOpts,
    libraryDependencies += typesafeConfig,
    libraryDependencies += kafkaClient,
    libraryDependencies += kstreams,
    libraryDependencies += kstreamsScala,
    libraryDependencies += jacksonScala,
    libraryDependencies += joda,
    libraryDependencies += awsSDK,
    libraryDependencies += metrics,
    libraryDependencies += metricsHeader,
    libraryDependencies += dropWizardMetrics,
    libraryDependencies += logback,
    //Test
    libraryDependencies += kstreamsTest % Test,
    libraryDependencies += scalaMock % Test ,
    libraryDependencies += scalaTest % Test
  )

//release
credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
releaseVersionBump := sbtrelease.Version.Bump.Minor
publishTo := {
   val artifactory = "https://artifactory-artifactory-01.inf.us-west-2.aws.plume.tech"
   if (isSnapshot.value)
      Some("Artifactory Realm" at artifactory + "/artifactory/sdata/scala-build-snapshots")
   else
      Some("Artifactory Realm"  at artifactory + "/artifactory/sdata/scala-build")
}
