package io.komoot.server.routes

import java.time.LocalDateTime

import cats.effect.kernel.Concurrent
import cats.implicits.{toFlatMapOps, toFunctorOps}
import cats.syntax.flatMap._
import io.komoot.aws.SNSAwsService
import io.komoot.models.NewUser
import io.komoot.models.aws.SnsMessage
import io.komoot.server.routes.specs.HttpNewUserSignupRouteSpec
import io.komoot.services.SignupNotifyService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request, Response}
import org.typelevel.log4cats.slf4j.Slf4jFactory

class HttpNewUserSignupRoute[F[_]: Concurrent](
  snsAwsService: SNSAwsService[F],
  signupNotifyService: SignupNotifyService[F]
)(implicit loggerFactory: Slf4jFactory[F])
    extends Http4sDsl[F] with HttpRoute[F] with HttpNewUserSignupRouteSpec[F] {

  private lazy val logger = loggerFactory.getLogger

  implicit val messageEntityDecoder = jsonOf[F, SnsMessage]
  implicit val newUserEntityDecoder = jsonOf[F, NewUser]

  override def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "api" / "new-user-signup" => notifyNewUserSignup(req)
    case req @ POST -> Root / "api" / "aws-sns" / "new-user-signup" => notifyNewUserSignupFromAwsSNS(req)
  }

  override def notifyNewUserSignup(req: Request[F]): F[Response[F]] = {
    for {
      _ <- logger.debug(s"call ~/new-user-signup endpoint...")
      newUser <- req.as[NewUser]
      _ <- signupNotifyService.notify(newUser, LocalDateTime.now())
      response <- NoContent()
    } yield response
  }

  override def notifyNewUserSignupFromAwsSNS(req: Request[F]): F[Response[F]] = {

    def process(snsMessage: SnsMessage): F[Unit] = {
      // The first message after subscribing to the ARN, contains the subscribeURL to confirm.
      snsMessage match {
        case SnsMessage(Some(newUser), _) => signupNotifyService.notify(newUser, LocalDateTime.now())
        case SnsMessage(_, Some(subscribeURL)) => {
          snsAwsService.confirmSubscribeURL(subscribeURL).ifM(
            logger.info("Confirm subscribe URL API OK."),
            logger.warn("Confirm subscribe URL API KO, please retry or contact support.")
          )
        }
        case value => logger.warn(s"Do nothing, incorrect data.\n\r$value").as(())
      }
    }

    for {
      bodyRequest <- req.as[String]
      _ <- logger.debug(s"call ~/aws-sns/new-user-signup endpoint...\n\r$bodyRequest")
      snsMessage <- req.as[SnsMessage]
      _ <- process(snsMessage)
      response <- NoContent()
    } yield response
  }
}
