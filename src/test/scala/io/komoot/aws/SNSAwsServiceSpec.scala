package io.komoot.aws

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import io.komoot.{AppHelpers, EnvHelpers}

import software.amazon.awssdk.http.SdkHttpResponse
import software.amazon.awssdk.services.sns.model.{SubscribeRequest, SubscribeResponse}

class SNSAwsServiceSpec extends AppHelpers with EnvHelpers {

  "SNSAwsServiceSpec" when {

    "subscribe" must {

      def buildResponse(statusCode: Int): IO[SubscribeResponse] = {
        SubscribeResponse.builder().sdkHttpResponse(
          SdkHttpResponse.builder().statusCode(statusCode).build()
        ).build().asInstanceOf[SubscribeResponse].pure[IO]
      }

      "return 'true' if the client is connected to the ARN:SNS" in {
        val snsClient = mock[SnsClientA]
        snsClient.subscribe(*[SubscribeRequest]).returns(buildResponse(200))

        val service = new SNSAwsService(snsClient, null)

        val result = service.subscribe("topic:arn", "endpoint").unsafeRunSync()

        result mustBe true

        snsClient.subscribe(SubscribeRequest.builder()
          .protocol("http")
          .endpoint("endpoint")
          .returnSubscriptionArn(true)
          .topicArn("topic:arn")
          .build()).wasCalled(once)
      }

      "return 'false' if the client fails to connect to the ARN:SNS" in {
        val snsClient = mock[SnsClientA]
        snsClient.subscribe(*[SubscribeRequest]).returns(buildResponse(409))

        val service = new SNSAwsService(snsClient, null)

        service.subscribe("topic:arn", "endpoint").unsafeRunSync() mustBe false
      }
    }
  }
}
