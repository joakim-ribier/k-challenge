package io.komoot.models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class UserNotification(sender: String, receiver: Long, message: String, recent_user_ids: List[Long])

object UserNotification {

  implicit val codec: Codec[UserNotification] = deriveCodec[UserNotification]
}
