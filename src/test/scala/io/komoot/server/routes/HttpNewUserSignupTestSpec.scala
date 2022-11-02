package io.komoot.server.routes

import java.time.LocalDateTime

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import io.circe.Json
import io.circe.syntax.EncoderOps
import io.komoot.aws.SNSAwsService
import io.komoot.models.NewUser
import io.komoot.services.SignupNotifyService
import io.komoot.{AppHelpers, EnvHelpers}
import org.http4s.Method.POST
import org.http4s.Request
import org.http4s.Status.NoContent
import org.http4s.Uri.unsafeFromString
import org.http4s.circe._

class HttpNewUserSignupTestSpec extends AppHelpers with EnvHelpers {

  "HttpNewUserSignup" when {

    "call ~/api/new-user-signup" must {

      "post a new signup user" in {
        val request: Request[IO] =
          Request(POST, unsafeFromString("/api/new-user-signup")).withEntity(marcus.newUser.asJson)

        val signupNotifyService = mock[SignupNotifyService]
        signupNotifyService.notify(*[NewUser], *[LocalDateTime]).returns(().pure[IO])

        val service = new HttpNewUserSignupRoute(null, signupNotifyService)

        val (status, _) = run(service, request)

        status mustBe NoContent

        signupNotifyService.notify(marcus.newUser, *[LocalDateTime]).wasCalled(once)
      }
    }

    "call ~/api/aws-sns/new-user-signup" must {

      "post a new signup user from the ARN:SNS 'Message'" in {
        val request: Request[IO] =
          Request(POST, unsafeFromString("/api/aws-sns/new-user-signup")).withEntity(
            Json.obj("Message" -> Json.fromString(marcus.newUser.asJson.noSpaces))
          )

        val signupNotifyService = mock[SignupNotifyService]
        signupNotifyService.notify(*[NewUser], *[LocalDateTime]).returns(().pure[IO])

        val service = new HttpNewUserSignupRoute(null, signupNotifyService)

        val (status, _) = run(service, request)

        status mustBe NoContent

        signupNotifyService.notify(marcus.newUser, *[LocalDateTime]).wasCalled(once)
      }

      "confirm the 'SubscribeURL' for the very first message from the ARN:SNS" in {
        val request: Request[IO] =
          Request(POST, unsafeFromString("/api/aws-sns/new-user-signup")).withEntity(
            Json.obj(
              "Message" -> Json.fromString("You have chosen to subscri..."),
              "SubscribeURL" -> Json.fromString("{url to confirm}")
            )
          )

        val snsAwsService = mock[SNSAwsService]
        snsAwsService.confirmSubscribeURL(*[String]).returns(true.pure[IO])

        val service = new HttpNewUserSignupRoute(snsAwsService, null)

        val (status, _) = run(service, request)

        status mustBe NoContent

        snsAwsService.confirmSubscribeURL("{url to confirm}").wasCalled(once)
      }
    }
  }
}
