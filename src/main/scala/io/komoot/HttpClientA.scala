package io.komoot

import cats.effect.Concurrent
import org.http4s.Request
import org.http4s.client.Client

class HttpClientA[F[_]: Concurrent](httpClient: Client[F]) {

  def successful(request: Request[F]): F[Boolean] = {
    httpClient.successful(request)
  }
}
