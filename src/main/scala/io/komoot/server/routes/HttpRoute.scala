package io.komoot.server.routes

import org.http4s.HttpRoutes

trait HttpRoute[F[_]] {

  def routes: HttpRoutes[F]
}
