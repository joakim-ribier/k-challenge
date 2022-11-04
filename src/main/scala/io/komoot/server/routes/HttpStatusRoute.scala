package io.komoot.server.routes

import buildinfo.BuildInfo
import cats.Monad
import cats.implicits.{toFlatMapOps, toFunctorOps}
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import io.komoot.server.routes.specs.HttpStatusRouteSpec
import io.komoot.services.NewUserCacheService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import org.typelevel.log4cats.slf4j.Slf4jFactory

class HttpStatusRoute[F[_]: Monad](newUserCacheService: NewUserCacheService[F])(implicit loggerFactory: Slf4jFactory[F])
    extends Http4sDsl[F] with HttpRoute[F] with HttpStatusRouteSpec[F] {

  implicit def anyWithEncoderToJsValue[T](field: T)(implicit enc: Encoder[T]): Json = enc(field)

  private lazy val logger = loggerFactory.getLogger

  override def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "api" / "status" => status()
    case GET -> Root / "api" / "status" / "cache" => cache()
  }

  override def cache(): F[Response[F]] = {
    newUserCacheService.getCache().flatMap(f => Ok(f.newUserList.asJson))
  }

  override def status(): F[Response[F]] = {
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
