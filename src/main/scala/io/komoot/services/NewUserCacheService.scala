package io.komoot.services

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt

import cats.effect.IO
import io.komoot.models.NewUser
import io.komoot.models.cache.NewUserData

import scalacache.Cache

class NewUserCacheService(
  cache: Cache[IO, String, NewUserData]) {

  private val key = "new-user-list"

  def getCache(): IO[NewUserData] = cache.get(key).map(_.getOrElse(NewUserData.empty))

  def update(newUser: NewUser, notifySuccessful: Boolean, receivedAt: LocalDateTime): IO[Unit] = {
    for {
      data <- getCache()
      _ <- cache.put(key)(data.add(newUser, notifySuccessful, receivedAt), Some(2.hour))
    } yield ()
  }
}
