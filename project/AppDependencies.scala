import sbt._

object AppDependencies {

  private val playVersion = "play-30"
  private val bootstrapVersion = "10.3.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"            %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"            %% s"domain-$playVersion"            % "13.0.0",
    "com.github.fge"         %  "json-schema-validator"           % "2.2.6",
    "org.scala-lang.modules" %% "scala-parallel-collections"      % "1.2.0",
    "uk.gov.hmrc"            %% "stub-data-generator"             % "1.5.0",
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion" % bootstrapVersion,
    "org.scalatestplus"      %% "scalacheck-1-17"              % "3.2.18.0",
    "org.scalatestplus.play" %% "scalatestplus-play"           % "7.0.2",
    "uk.gov.hmrc"            %% "stub-data-generator"          % "1.5.0",
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
