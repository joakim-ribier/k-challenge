package io.komoot.server.routes

import cats.effect.IO
import io.circe.syntax.EncoderOps
import io.komoot.MatcherHelpers.JsonMatchers
import io.komoot.models.cache.NewUserData
import io.komoot.services.NewUserCacheService
import io.komoot.{AppHelpers, EnvHelpers}
import org.http4s.Method.GET
import org.http4s.Request
import org.http4s.Status.Ok
import org.http4s.Uri.unsafeFromString

class HttpStatusRouteTestSpec extends AppHelpers with EnvHelpers {

  "HttpStatusRoute" when {

    "call ~/api/status" must {

      "return info on the status of the application" in {
        val request: Request[IO] = Request(GET, unsafeFromString("/api/status"))

        val service = new HttpStatusRoute(null)

        val (status, maybeBody) = run(service, request)

        status mustBe Ok

        maybeBody.get must JsonMatchers.contains("name", "komoot-challenge")
        maybeBody.get must JsonMatchers.contains("version", "0.1.0-SNAPSHOT")
      }

      "return the state of the current cache" in {
        val request: Request[IO] = Request(GET, unsafeFromString("/api/status/cache"))

        val newUserCacheService = mock[NewUserCacheService]
        newUserCacheService.getCache().returns(IO.pure(NewUserData(List(marcus))))

        val service = new HttpStatusRoute(newUserCacheService)

        val (status, maybeBody) = run(service, request)

        status mustBe Ok

        maybeBody.get mustBe List(marcus).asJson
      }
    }
  }
}
