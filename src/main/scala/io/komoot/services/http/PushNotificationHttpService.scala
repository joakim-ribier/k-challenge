package io.komoot.services.http

import cats.effect.IO
import io.circe.syntax.EncoderOps
import io.komoot.HttpClientA
import io.komoot.config.KomootConfig
import io.komoot.models.UserNotification
import org.http4s.Method.POST
import org.http4s.Request
import org.http4s.Uri.unsafeFromString
import org.http4s.circe._
import org.typelevel.log4cats.slf4j.Slf4jFactory

class PushNotificationHttpService(
  httpClient: HttpClientA,
  config: KomootConfig
)(implicit loggerFactory: Slf4jFactory[IO]) {

  private lazy val logger = loggerFactory.getLogger

  def push(value: UserNotification): IO[Boolean] = {
    val req = Request[IO](POST)
      .withUri(unsafeFromString(config.api))
      .withEntity(value.asJson)

    httpClient.successful(req).handleErrorWith(
      logger
        .error(_)("Cannot post payload to Komoot API.")
        .as(false)
    )
  }
}
