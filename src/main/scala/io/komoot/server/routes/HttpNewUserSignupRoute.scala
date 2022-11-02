package io.komoot.server.routes

import java.time.LocalDateTime

import cats.effect.IO
import io.komoot.aws.SNSAwsService
import io.komoot.models.NewUser
import io.komoot.models.aws.SnsMessage
import io.komoot.server.routes.specs.HttpNewUserSignupRouteSpec
import io.komoot.services.SignupNotifyService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request, Response}
import org.typelevel.log4cats.slf4j.Slf4jFactory

class HttpNewUserSignupRoute(
  snsAwsService: SNSAwsService,
  signupNotifyService: SignupNotifyService
)(implicit loggerFactory: Slf4jFactory[IO])
    extends Http4sDsl[IO] with HttpRoute with HttpNewUserSignupRouteSpec {

  private lazy val logger = loggerFactory.getLogger

  implicit val messageEntityDecoder = jsonOf[IO, SnsMessage]
  implicit val newUserEntityDecoder = jsonOf[IO, NewUser]

  override def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "api" / "new-user-signup" => notifyNewUserSignup(req)
    case req @ POST -> Root / "api" / "aws-sns" / "new-user-signup" => notifyNewUserSignupFromAwsSNS(req)
  }

  override def notifyNewUserSignup(req: Request[IO]): IO[Response[IO]] = {
    for {
      _ <- logger.debug(s"call ~/new-user-signup endpoint...")
      newUser <- req.as[NewUser]
      _ <- signupNotifyService.notify(newUser, LocalDateTime.now())
      response <- NoContent()
    } yield response
  }

  override def notifyNewUserSignupFromAwsSNS(req: Request[IO]): IO[Response[IO]] = {

    def process(snsMessage: SnsMessage): IO[Unit] = {
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
