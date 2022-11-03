package io.komoot.aws

import cats.effect.IO
import io.komoot.config.Config

import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.{SubscribeRequest, SubscribeResponse}

class SnsClientA(config: Config) {

  lazy val client: IO[SnsClient] = {
    IO.pure {
      val awsCreds = AwsBasicCredentials.create(config.aws.accessKeyId, config.aws.secretAccessKey)
      SnsClient.builder().region(Region.of(config.komoot.region)).credentialsProvider(
        StaticCredentialsProvider.create(awsCreds)
      ).build()
    }
  }

  def subscribe(request: SubscribeRequest): IO[SubscribeResponse] = {
    client.map(_.subscribe(request))
  }
}
