package io.komoot.config

import cats.effect._
import com.typesafe.config.ConfigFactory

import pureconfig._

final case class Config(
  sender: String,
  snsEndpointToReceiveNotification: String,
  nbRecentlyUsersMaxToKeep: Int,
  aws: AwsConfig,
  komoot: KomootConfig,
  http: HttpConfig)

class ConfigLoader {

  def load(): Resource[IO, Config] = {
    import pureconfig.generic.auto._
    import pureconfig.module.catseffect.syntax._

    Resource.eval(ConfigSource.fromConfig(ConfigFactory.load()).loadF[IO, Config]())
  }
}
