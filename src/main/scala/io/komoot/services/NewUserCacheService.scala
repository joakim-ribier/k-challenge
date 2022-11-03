package io.komoot.services

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt

import cats.Monad
import cats.implicits.{toFlatMapOps, toFunctorOps}
import io.komoot.models.NewUser
import io.komoot.models.cache.NewUserData

import scalacache.Cache

class NewUserCacheService[F[_]: Monad](
  cache: Cache[F, String, NewUserData]) {

  private val key = "new-user-list"

  def getCache(): F[NewUserData] = cache.get(key).map(_.getOrElse(NewUserData.empty))

  def update(newUser: NewUser, notifySuccessful: Boolean, receivedAt: LocalDateTime): F[Unit] = {
    for {
      data <- getCache()
      _ <- cache.put(key)(data.add(newUser, notifySuccessful, receivedAt), Some(2.hour))
    } yield ()
  }
}
