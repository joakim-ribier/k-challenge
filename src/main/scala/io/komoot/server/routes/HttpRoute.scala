package io.komoot.server.routes

import cats.effect.IO
import org.http4s.HttpRoutes

trait HttpRoute {

  def routes: HttpRoutes[IO]
}
