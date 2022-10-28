import Dependencies.Versions.{awsSdkVersion, circeVersion, http4sVersion, pureConfigVersion, scaffeineVersion, scalatestVersion}
import sbt._

object Dependencies {

  object Versions {
    val scalaVersion = "2.13.3"

    val http4sVersion = "1.0.0-M35"

    val pureConfigVersion = "0.17.1"
    val circeVersion = "0.14.2"
    val scalatestVersion = "3.2.12"
    val scaffeineVersion = "5.1.2"

    val awsSdkVersion = "2.18.4"
  }

  val resolvers = Seq(Resolver.jcenterRepo)

  object Library {

    val scalacache = "com.github.cb372" %% "scalacache-caffeine" % "1.0.0-M6"

    val http4s = Seq(
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-blaze-client",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-dsl"
    ).map(_ % http4sVersion)

    val logs = Seq(
      "org.typelevel" %% "log4cats-slf4j" % "2.4.0",
      "ch.qos.logback" % "logback-classic" % "1.2.11",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.2"
    )

    val pureconfig = Seq(
      "com.github.pureconfig" %% "pureconfig-generic",
      "com.github.pureconfig" %% "pureconfig-cats-effect"
    ).map(_ % pureConfigVersion)

    val circe = Seq(
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-optics" % "0.14.1",
      "io.circe" %% "circe-fs2" % "0.14.0"
    )

    val scalaTest = Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion % Test,
      "org.mockito" %% "mockito-scala" % "1.17.7" % Test
    )

    val aws = Seq(
      "software.amazon.awssdk" % "auth" % awsSdkVersion,
      "software.amazon.awssdk" % "sns" % awsSdkVersion
    )
  }
}
