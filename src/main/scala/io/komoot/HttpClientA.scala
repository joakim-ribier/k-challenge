package io.komoot

import cats.effect.IO
import org.http4s.Request
import org.http4s.client.Client

class HttpClientA(httpClient: Client[IO]) {

  def successful(request: Request[IO]): IO[Boolean] = {
    httpClient.successful(request)
  }
}
