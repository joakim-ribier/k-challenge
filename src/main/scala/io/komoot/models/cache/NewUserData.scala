package io.komoot.models.cache

import java.time.LocalDateTime

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.komoot.models.NewUser

case class NewUserInfo(newUser: NewUser, pushNotification: Boolean, receivedMessageAt: LocalDateTime) {
  val id = newUser.id
  val createdAt = newUser.created_at
  val name = newUser.name
}

object NewUserInfo {

  implicit val codec: Codec[NewUserInfo] = deriveCodec[NewUserInfo]
}

case class NewUserData(newUserList: List[NewUserInfo]) {

  def exists(newUser: NewUser): Boolean = newUserList.exists(_.id == newUser.id)

  def add(value: NewUser, pushTo: Boolean, receivedAt: LocalDateTime): NewUserData = {
    if (exists(value)) this
    else copy(newUserList = newUserList ++ List(NewUserInfo(value, pushTo, receivedAt)))
  }
}

object NewUserData {

  val empty = NewUserData(List.empty[NewUserInfo])
}
