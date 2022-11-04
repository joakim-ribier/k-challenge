package io.komoot.services.http

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import io.komoot.config.KomootConfig
import io.komoot.{AppHelpers, EnvHelpers, HttpClientA}
import org.http4s.Request

class PushNotificationHttpSpec extends AppHelpers with EnvHelpers {

  "PushNotificationHttp" when {

    "push" must {

      "posts notification to an specific endpoint" in {
        val httpClientA = mock[HttpClientA[IO]]
        httpClientA.successful(*[Request[IO]]).returns(true.pure[IO])

        val service =
          new PushNotificationHttpService(httpClientA, KomootConfig("{sns}", "http://www.komoot.fr", false, "{region}"))

        val result = service.push(userNotification).unsafeRunSync()

        result mustBe true
      }

      "return 'false' is an error occurred!" in {
        val httpClientA = mock[HttpClientA[IO]]
        httpClientA.successful(*[Request[IO]]).returns(IO.raiseError(new Throwable("error test")))

        val service =
          new PushNotificationHttpService(httpClientA, KomootConfig("{sns}", "http://www.komoot.fr", false, "{region}"))

        val result = service.push(userNotification).unsafeRunSync()

        result mustBe false
      }
    }
  }
}
