
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

// ================================================================================
// Testing
// ================================================================================
libraryDependencies ++= Seq(
  "org.mockito"             %  "mockito-core"            % "3.11.0",
  "org.pegdown"             %  "pegdown"                 % "1.6.0",
  "org.scalatestplus.play"  %% "scalatestplus-play"      % "5.0.0"
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
scalaVersion := "2.12.13"

libraryDependencies ++= Seq(
  ws,
  "com.github.fge"          %  "json-schema-validator"      % "2.2.6",
  "org.scalacheck"          %% "scalacheck"                 % "1.15.4",
  "uk.gov.hmrc"             %% "domain"                     % "5.11.0-play-27",
  "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.3.0",
  "uk.gov.hmrc"             %% "play-ui"                    % "9.5.0-play-28",
  "uk.gov.hmrc"             %% "stub-data-generator"        % "0.5.3",
  compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.5" cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % "1.7.5" % Provided cross CrossVersion.full
)

// ================================================================================
// Compiler flags
// ================================================================================

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-language:implicitConversions",
//  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.  
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
//  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
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