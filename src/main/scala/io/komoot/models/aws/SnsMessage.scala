package io.komoot.models.aws

import io.circe.{Decoder, HCursor, parser}
import io.komoot.models.NewUser

case class SnsMessage(maybeNewUser: Option[NewUser], maybeSubscribeURL: Option[String])

object SnsMessage {

  implicit val decoder: Decoder[SnsMessage] = Decoder.instance((c: HCursor) =>
    for {
      message <- c.get[String]("Message")
      maybeNewUser = parser.parse(message).toOption.flatMap { json =>
        json.as[NewUser] match {
          case Left(_) => Option.empty[NewUser]
          case Right(newUser) => Some(newUser)
        }
      }
      maybeSubscribeURL <- c.get[Option[String]]("SubscribeURL")
    } yield SnsMessage(maybeNewUser, maybeSubscribeURL)
  )
}
