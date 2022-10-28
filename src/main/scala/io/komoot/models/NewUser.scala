package io.komoot.models

import java.time.LocalDateTime

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class NewUser(name: String, id: Long, created_at: LocalDateTime)

object NewUser {

  implicit val codec: Codec[NewUser] = deriveCodec[NewUser]
}
