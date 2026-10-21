val scala3Version = "3.9.0"
val tapirVersion = "1.14.0"
val http4sVersion = "0.23.38"
val circeVersion = "0.14.17"
val munitCEVersion = "2.2.1"
val logbackVersion = "1.6.5"

lazy val root = (project in file("."))
  .settings(
    scalaVersion := scala3Version,
    idePackagePrefix := Some("dev.tcoustic"),
    name := "scala-contract-service",
    libraryDependencies ++= Seq(
      // Tapir & Http4s
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,

      // Testing Dependencies (QA / SDET layer)
      "org.typelevel" %% "munit-cats-effect" % munitCEVersion % Test
    )
  )


