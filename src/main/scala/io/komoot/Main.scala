package io.komoot

import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.{IO, IOApp, Resource}
import io.komoot.aws.SNSAwsService
import io.komoot.config.ConfigLoader
import io.komoot.models.cache.NewUserData
import io.komoot.server.AppServer
import io.komoot.services.http.PushNotificationHttpService
import io.komoot.services.{NewUserCacheService, SignupNotifyService}
import org.http4s.blaze.client.BlazeClientBuilder
import org.typelevel.log4cats.slf4j.{Slf4jFactory, loggerFactoryforSync}

import scalacache.caffeine.CaffeineCache
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient

object Main extends IOApp.Simple {

  override def run: IO[Unit] = {

    implicit val loggerFactory: Slf4jFactory[IO] = loggerFactoryforSync[IO]
    lazy val logger = loggerFactory.getLogger

    (for {
      config <- new ConfigLoader().load()
      _ <- Resource.eval(logger.info(s"Starting application.\n\r$config"))

      httpClient <- BlazeClientBuilder[IO].resource

      // aws
      awsCreds = AwsBasicCredentials.create(config.aws.accessKeyId, config.aws.secretAccessKey)
      snsClient = SnsClient.builder().region(Region.of(config.komoot.region)).credentialsProvider(
        StaticCredentialsProvider.create(awsCreds)
      ).build()
      _ <- Resource.eval(new SNSAwsService(snsClient).subscribe(
        config.komoot.sns,
        config.snsEndpointToReceiveNotification
      ))

      newUserCacheService <- Resource.eval(CaffeineCache[IO, String, NewUserData]).map(new NewUserCacheService(_))
      pushNotificationHttpService = new PushNotificationHttpService(httpClient, config.komoot)
      signupNotifyService = new SignupNotifyService(config, newUserCacheService, pushNotificationHttpService)

      appServer = new AppServer(config.http, signupNotifyService, httpClient, newUserCacheService)
      _ <- appServer.start()
    } yield ()).useForever
  }
}
