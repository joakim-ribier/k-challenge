package io.komoot.config

import cats.effect.IO
import io.komoot.{AppHelpers, EnvHelpers}

class ConfigSpec extends AppHelpers with EnvHelpers {

  "Config" must {

    "load 'application.conf' file" in {
      val config = new ConfigLoader().load().use(IO.pure).unsafeRunSync()

      config.aws mustBe AwsConfig("{access-key-id}", "{secret-access-key}")
    }
  }
}
