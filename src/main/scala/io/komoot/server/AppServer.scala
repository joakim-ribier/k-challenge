package io.komoot.server

import scala.concurrent.ExecutionContext

import cats.effect.{IO, Resource}
import cats.implicits.toSemigroupKOps
import io.komoot.config.HttpConfig
import io.komoot.server.routes.{HttpNewUserSignupRoute, HttpStatusRoute}
import io.komoot.services.{NewUserCacheService, SignupNotifyService}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.middleware.CORS
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.slf4j.Slf4jFactory

class AppServer(
  httpConfig: HttpConfig,
  signupNotifyService: SignupNotifyService,
  httpClient: Client[IO],
  newUserCacheService: NewUserCacheService
)(implicit ec: ExecutionContext,
  loggerFactory: Slf4jFactory[IO])
    extends Http4sDsl[IO] {

  lazy val httpStatusRoute = new HttpStatusRoute(newUserCacheService)
  lazy val httpNewUserSignupRoute = new HttpNewUserSignupRoute(httpClient, signupNotifyService)

  def start(): Resource[IO, Server] = {
    val corsPolicy = CORS.policy.withAllowOriginAll.withAllowCredentials(false)
    val services = corsPolicy(httpStatusRoute.routes) <+> corsPolicy(httpNewUserSignupRoute.routes)

    val httpApp = Router("/" -> services).orNotFound

    BlazeServerBuilder[IO]
      .withExecutionContext(ec)
      .bindHttp(httpConfig.port, httpConfig.host)
      .withHttpApp(httpApp)
      .resource
  }
}
