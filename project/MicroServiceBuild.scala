import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object MicroServiceBuild extends Build with MicroService {

  val appName = "soft-drinks-industry-levy-stub"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "stub-data-generator" % "0.5.3",
    "uk.gov.hmrc" %% "microservice-bootstrap" % "6.18.0",
    "uk.gov.hmrc" %% "play-ui" % "7.22.0",
    "uk.gov.hmrc" %% "domain" % "5.2.0",
    "com.github.fge" % "json-schema-validator" % "2.2.6",
    "org.scalacheck" %% "scalacheck" % "1.14.0"
  )

  def test(scope: String = "test,it") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
    "org.scalatest" %% "scalatest" % "3.0.5" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.mockito" % "mockito-core" % "2.19.1" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope
  )

}
