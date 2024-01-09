import sbt._

object AppDependencies {

  val playVersion = "7.19.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "com.github.fge"         %  "json-schema-validator"      % "2.2.6",
    "uk.gov.hmrc"            %% "domain"                     % "8.3.0-play-28",
    "uk.gov.hmrc"            %% "bootstrap-backend-play-28"  % playVersion,
    "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
    "uk.gov.hmrc"            %% "stub-data-generator"        % "1.1.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"          %% "mockito-scala"          % "1.17.14",
    "org.scalatestplus"    %% "scalacheck-1-15"        % "3.2.11.0",
    "org.scalatest"        %% "scalatest"              % "3.2.16",
    "org.scalacheck"       %% "scalacheck"             % "1.17.0",
    "uk.gov.hmrc"          %% "bootstrap-test-play-28" % playVersion,
    "com.vladsch.flexmark" %  "flexmark-all"           % "0.64.8"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}