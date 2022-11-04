package io.komoot.aws

import cats.effect.Concurrent
import cats.implicits.toFlatMapOps
import cats.syntax.applicativeError._
import cats.syntax.functor._
import io.komoot.HttpClientA
import org.http4s.Method.GET
import org.http4s.Request
import org.http4s.Uri.unsafeFromString
import org.typelevel.log4cats.slf4j.Slf4jFactory

import software.amazon.awssdk.services.sns.model.SubscribeRequest

class SNSAwsService[F[_]: Concurrent](
  snsClient: SnsClientA[F],
  httpClient: HttpClientA[F]
)(implicit loggerFactory: Slf4jFactory[F]) {

  private lazy val logger = loggerFactory.getLogger

  def subscribe(topicArn: String, redirectionEndpoint: String): F[Boolean] = {
    val request: SubscribeRequest = SubscribeRequest.builder()
      .protocol("http")
      .endpoint(redirectionEndpoint)
      .returnSubscriptionArn(true)
      .topicArn(topicArn)
      .build()

    for {
      _ <- logger.info("Try to subscribe to SNS...")
      response <- snsClient.subscribe(request)
      result <- {
        if (response.sdkHttpResponse.isSuccessful) {
          logger.info("Subscription to SNS Ok.").as(true)
        } else logger.error("Subscription to SNS KO, please contact support.").as(false)
      }
    } yield result
  }

  def confirmSubscribeURL(subscribeURL: String): F[Boolean] = {
    val req = Request[F](GET)
      .withUri(unsafeFromString(subscribeURL))

    httpClient.successful(req).handleErrorWith(
      logger.error(_)(s"An error occurred on the confirm subscribe URL API.\n\r$subscribeURL").as(false)
    )
  }
}
