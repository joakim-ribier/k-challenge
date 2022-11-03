package io.komoot.aws

import cats.Applicative
import cats.syntax.functor._
import io.komoot.config.Config

import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.{SubscribeRequest, SubscribeResponse}

class SnsClientA[F[_]: Applicative](config: Config) {

  lazy val client: F[SnsClient] = {
    Applicative[F].pure {
      val awsCreds = AwsBasicCredentials.create(config.aws.accessKeyId, config.aws.secretAccessKey)
      SnsClient.builder().region(Region.of(config.komoot.region)).credentialsProvider(
        StaticCredentialsProvider.create(awsCreds)
      ).build()
    }
  }

  def subscribe(request: SubscribeRequest): F[SubscribeResponse] = {
    client.map(_.subscribe(request))
  }
}
