package io.komoot

import java.time.LocalDateTime
import java.time.LocalDateTime._

import io.komoot.models.NewUser
import io.komoot.models.cache.{NewUserData, NewUserInfo}
import io.komoot.services.SignupNotifyService

trait ModelSpec {

  def buildNewUserInfo(newUser: NewUser) = NewUserInfo(newUser, true, LocalDateTime.now())

  val marcus = buildNewUserInfo(NewUser("Marcus", 1, parse("2020-05-12T16:11:54.000")))
  val lydia = buildNewUserInfo(NewUser("Lydia", 2, parse("2020-05-13T16:11:54.000")))
  val lucas = buildNewUserInfo(NewUser("Lucas", 3, parse("2020-05-14T16:11:54.000")))
  val patrick = buildNewUserInfo(NewUser("Patrick", 4, parse("2020-05-15T16:11:54.000")))

  val userNotification =
    SignupNotifyService.buildNotification("test@komoot.io", marcus.newUser, NewUserData(List(lydia)))
}
