
// ================================================================================
// Plugins
// ================================================================================
enablePlugins(
  play.sbt.PlayScala,
  SbtDistributablesPlugin
)

// ================================================================================
// Play configuration
// ================================================================================
PlayKeys.playDefaultPort := 8702
val playVersion = "7.19.0"

// ================================================================================
// Testing
// ================================================================================
libraryDependencies ++= Seq(
  "org.mockito"             %% "mockito-scala"           % "1.17.14",
  "org.scalatestplus"       %% "scalacheck-1-15"         % "3.2.11.0",
  "org.scalatest"           %% "scalatest"               % "3.2.16",
  "org.scalacheck"          %% "scalacheck"                 % "1.17.0",
  "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % playVersion,
  "com.vladsch.flexmark" % "flexmark-all" % "0.64.8"
).map(_ % "test") 

// ================================================================================
// Scala Fmt
// ================================================================================

import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
Compile / scalafmtOnCompile := true
Test / scalafmtOnCompile := true

// ================================================================================
// Dependencies
// ================================================================================
scalaVersion := "2.13.9"

libraryDependencies ++= Seq(
  ws,
  "com.github.fge"          %  "json-schema-validator"      % "2.2.6",
  "uk.gov.hmrc"             %% "domain"                     % "8.3.0-play-28",
  "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % playVersion,
  "uk.gov.hmrc"             %% "stub-data-generator"        % "1.1.0",
  "org.scala-lang.modules"  %% "scala-parallel-collections"  % "1.0.4",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.12" cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % "1.7.12" % Provided cross CrossVersion.full
)

// ================================================================================
// Compiler flags
// ================================================================================

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-language:implicitConversions",
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused",                     // Warn if an import selector is not referenced.
  "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
)

// ================================================================================
// Misc
// ================================================================================

Test / initialCommands := "import uk.gov.hmrc.softdrinksindustrylevystub.Report.findRegistrationWhere"
majorVersion := 0
uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
scalacOptions += "-P:silencer:pathFilters=routes"