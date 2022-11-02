package io.komoot.aws

import cats.effect.IO
import org.http4s.Method.GET
import org.http4s.Request
import org.http4s.Uri.unsafeFromString
import org.http4s.client.Client
import org.typelevel.log4cats.slf4j.Slf4jFactory

import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.SubscribeRequest

class SNSAwsService(snsClient: SnsClient, httpClient: Client[IO])(implicit loggerFactory: Slf4jFactory[IO]) {

  private lazy val logger = loggerFactory.getLogger

  def subscribe(topicArn: String, redirectionEndpoint: String): IO[Boolean] = {
    val request = SubscribeRequest.builder()
      .protocol("http")
      .endpoint(redirectionEndpoint)
      .returnSubscriptionArn(true)
      .topicArn(topicArn)
      .build()

    for {
      _ <- logger.info("Try to subscribe to SNS...")
      response = snsClient.subscribe(request)
      result <- {
        if (response.sdkHttpResponse.isSuccessful) {
          logger.info("Subscription to SNS Ok.").as(true)
        } else logger.error("Subscription to SNS KO, please contact support.").as(false)
      }
    } yield result
  }

  def confirmSubscribeURL(subscribeURL: String): IO[Boolean] = {
    val req = Request[IO](GET)
      .withUri(unsafeFromString(subscribeURL))

    httpClient.successful(req).handleErrorWith(
      logger.error(_)(s"An error occurred on the confirm subscribe URL API.\n\r$subscribeURL").as(false)
    )
  }
}
