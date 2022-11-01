import com.typesafe.sbt.packager.MappingsHelper.directory

import Dependencies.Library._
import Dependencies.{resolvers => myResolvers}
import sbt.Keys.resolvers

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "komoot-challenge",
    description := "Komoot Challenge 2022",
    organization := "komoot.io",
    scalaVersion := Dependencies.Versions.scalaVersion,
    libraryDependencies ++= circe ++ logs ++ http4s ++ scalaTest ++ aws ++ pureconfig ++ List(scalacache),
    BuildInfoSettings,
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8", // Specify character encoding used by source files.
      "-explaintypes", // Explain type errors in more detail.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-language:reflectiveCalls",
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds", // Allow higher-kinded types
      "-language:postfixOps",
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
      "-Ywarn-macros:after",
      "-Ymacro-annotations",
      "-Ywarn-numeric-widen", // Warn when numerics are widened.
      "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
      "-Ywarn-unused:params", // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:locals", // Warn if a local definition is unused.
      "-Ywarn-unused:privates", // Warn if a private member is unused.
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
      "-Xfatal-warnings", // Compilation error when there is a warning.
      "-Xlint:_,-missing-interpolator,-byname-implicit" // Additional warnings (see scalac -Xlint:help), except missing interpolator
    )
  )

Universal / mappings ++= directory("src/main/resources")

ThisBuild / resolvers ++= myResolvers

// API documentation
docsCleanBefore := true
docsMap := Map(file("src/main/scala/io/komoot/server/routes/specs") -> file("resources/specs"))

// coverage
ThisBuild / coverageEnabled := true
ThisBuild / coverageExcludedPackages := "<empty>;Reverse.*;aws\\.*;.*BuildInfo.*;.*RoutesPrefix.*;.*mock.*;.*Main.*;.*AppServer.*"
ThisBuild / coverageMinimumStmtTotal := 60
ThisBuild / coverageFailOnMinimum := true

// scalafmt
scalafmtOnCompile := false // Use "Format on save" instead https://scalameta.org/scalafmt/docs/installation.html#format-on-save
Test / testOptions += Tests.Argument("-oDF") // Show full stack traces in tests and duration for each test

lazy val BuildInfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    "gitCommitSHA" -> {
      scala.sys.process.Process("git rev-parse HEAD").lineStream.headOption.getOrElse("unknown")
    }
  ),
  buildInfoOptions += BuildInfoOption.BuildTime
)