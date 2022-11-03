package io.komoot.config

import cats.effect.SyncIO
import io.komoot.{AppHelpers, EnvHelpers}

class ConfigSpec extends AppHelpers with EnvHelpers {

  "Config" must {

    "load 'application.conf' file" in {
      val config = new ConfigLoader[SyncIO]().load().use(SyncIO.pure).unsafeRunSync()

      config.aws mustBe AwsConfig("{access-key-id}", "{secret-access-key}")
    }
  }
}
