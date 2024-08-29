import sbt._

object AppDependencies {

  private val playVersion = "-play-30"
  private val bootstrapVersion = "9.3.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "com.github.fge"         %  "json-schema-validator"         % "2.2.6",
    "uk.gov.hmrc"            %% s"domain$playVersion"            % "10.0.0",
    "uk.gov.hmrc"            %% s"bootstrap-backend$playVersion" % bootstrapVersion,
    "org.scala-lang.modules" %% "scala-parallel-collections"    % "1.0.4",
    "uk.gov.hmrc" %% "stub-data-generator" % "1.1.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"            %% "mockito-scala-scalatest"    % "1.17.37",
    "org.scalatestplus"      %% "scalacheck-1-17"            % "3.2.18.0",
    "org.scalatestplus.play" %% "scalatestplus-play"         % "7.0.1",
    "uk.gov.hmrc"            %% s"bootstrap-test$playVersion" % bootstrapVersion
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
