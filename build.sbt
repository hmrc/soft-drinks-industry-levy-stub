
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
// Scala Fmt
// ================================================================================

Compile / scalafmtOnCompile := true
Test / scalafmtOnCompile := true

// ================================================================================
// Dependencies
// ================================================================================
scalaVersion := "3.3.6"
libraryDependencies ++= AppDependencies()

// ================================================================================
// Compiler flags
// ================================================================================

scalacOptions --= Seq(
  "-deprecation",
  "-unchecked",
  "-encoding", "UTF-8"
)

scalacOptions += s"-Wconf:msg=unused import:s,msg=unused explicit parameter:s,src=.*[\\\\/]routes[\\\\/].*:s"

// ================================================================================
// Misc
// ================================================================================

Test / initialCommands := "import uk.gov.hmrc.softdrinksindustrylevystub.Report.findRegistrationWhere"
majorVersion := 0
libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427