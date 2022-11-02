package io.komoot.server.routes

import buildinfo.BuildInfo
import cats.effect.IO
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import io.komoot.server.routes.specs.HttpStatusRouteSpec
import io.komoot.services.NewUserCacheService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import org.typelevel.log4cats.slf4j.Slf4jFactory

class HttpStatusRoute(newUserCacheService: NewUserCacheService)(implicit loggerFactory: Slf4jFactory[IO])
    extends Http4sDsl[IO] with HttpRoute with HttpStatusRouteSpec {

  implicit def anyWithEncoderToJsValue[T](field: T)(implicit enc: Encoder[T]): Json = enc(field)

  private lazy val logger = loggerFactory.getLogger

  override def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "api" / "status" => status()
    case GET -> Root / "api" / "status" / "cache" => cache()
  }

  override def cache(): IO[Response[IO]] = {
    newUserCacheService.getCache().flatMap(f => Ok(f.newUserList.asJson))
  }

  override def status(): IO[Response[IO]] = {
    for {
      _ <- logger.debug("call ~/status endpoint...")
      r <- Ok(Json.obj(
        "name" -> BuildInfo.name,
        "commit" -> BuildInfo.gitCommitSHA,
        "version" -> BuildInfo.version,
        "build" -> BuildInfo.builtAtString
      ))
    } yield r
  }
}
