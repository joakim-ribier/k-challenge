package io.komoot

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.circe.Json
import io.komoot.server.routes.HttpRoute
import org.http4s.Status.NoContent
import org.http4s.circe.jsonOf
import org.http4s.{Request, Status}
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.{LoggerName, SelfAwareStructuredLogger}

trait EnvHelpers {
  self: AppHelpers =>

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  def run(service: HttpRoute[IO], request: Request[IO]): (Status, Option[Json]) = {
    implicit val json = jsonOf[IO, Json]

    val response = service.routes.run(request).value.unsafeRunSync().get
    response.status match {
      case NoContent => (NoContent, Option.empty[Json])
      case status => (status, Some(response.as[Json].unsafeRunSync()))
    }
  }

  // mock logger
  implicit val loggerFactory: Slf4jFactory[IO] = mock[Slf4jFactory[IO]]
  val logger = mock[SelfAwareStructuredLogger[IO]]

  loggerFactory.getLogger(*[LoggerName]).returns(logger)

  logger.warn(*[String]).returns(IO.unit)
  logger.debug(*[String]).returns(IO.unit)
  logger.info(*[String]).returns(IO.unit)
  logger.error(*[String]).returns(IO.unit)
  logger.error(*[scala.Throwable])(*[String]).returns(IO.unit)
}
