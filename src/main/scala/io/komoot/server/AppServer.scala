package io.komoot.server

import scala.concurrent.ExecutionContext

import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits.toSemigroupKOps
import io.komoot.aws.SNSAwsService
import io.komoot.config.HttpConfig
import io.komoot.server.routes.{HttpNewUserSignupRoute, HttpStatusRoute}
import io.komoot.services.{NewUserCacheService, SignupNotifyService}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.middleware.CORS
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.slf4j.Slf4jFactory

class AppServer[F[_]: Async](
  httpConfig: HttpConfig,
  signupNotifyService: SignupNotifyService[F],
  newUserCacheService: NewUserCacheService[F],
  snsAwsService: SNSAwsService[F]
)(implicit ec: ExecutionContext,
  loggerFactory: Slf4jFactory[F])
    extends Http4sDsl[F] {

  lazy val httpStatusRoute = new HttpStatusRoute(newUserCacheService)
  lazy val httpNewUserSignupRoute = new HttpNewUserSignupRoute(snsAwsService, signupNotifyService)

  def start(): Resource[F, Server] = {
    val corsPolicy = CORS.policy.withAllowOriginAll.withAllowCredentials(false)
    val services = corsPolicy(httpStatusRoute.routes) <+> corsPolicy(httpNewUserSignupRoute.routes)

    val httpApp = Router("/" -> services).orNotFound

    BlazeServerBuilder[F]
      .withExecutionContext(ec)
      .bindHttp(httpConfig.port, httpConfig.host)
      .withHttpApp(httpApp)
      .resource
  }
}
