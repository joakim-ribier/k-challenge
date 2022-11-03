package io.komoot.services

import java.time.LocalDateTime

import cats.effect.kernel.Concurrent
import cats.implicits.{catsSyntaxApplicativeId, toFlatMapOps, toFunctorOps}
import io.komoot.config.Config
import io.komoot.models.cache.NewUserData
import io.komoot.models.{NewUser, UserNotification}
import io.komoot.services.SignupNotifyService.buildNotification
import io.komoot.services.http.PushNotificationHttpService
import org.typelevel.log4cats.slf4j.Slf4jFactory

class SignupNotifyService[F[_]: Concurrent](
  config: Config,
  newUserCacheService: NewUserCacheService[F],
  pushNotificationHttpService: PushNotificationHttpService[F]
)(implicit loggerFactory: Slf4jFactory[F]) {

  private lazy val logger = loggerFactory.getLogger

  def notify(newUser: NewUser, receivedAt: LocalDateTime): F[Unit] = {

    def push(newUserData: NewUserData): F[Unit] = {
      val userNotification = buildNotification(config.sender, newUser, newUserData, config.nbRecentlyUsersMaxToKeep)
      for {
        _ <- logger.info(s"Notify user ${newUser.name}, that he is welcome to our platform.")
        notifySuccessful <- {
          if (config.komoot.enable) { // for local test to avoid spam komoot API
            pushNotificationHttpService.push(userNotification)
          } else true.pure[F]
        }
        _ <- {
          (if (notifySuccessful) {
             logger.info(s"The notification has been sent.\n\r$userNotification")
           } else logger.warn("The notification has not been sent, please try again or contact support.")).flatMap(_ =>
            newUserCacheService.update(newUser, notifySuccessful, receivedAt)
          )
        }
      } yield ()
    }

    for {
      newUserData <- newUserCacheService.getCache()
      _ <- {
        if (newUserData.exists(newUser)) { // to avoid spam, do not send again notification if user already exists
          logger.info(s"Do not notify user ${newUser.name} again, he already exists in our platform.")
        } else push(newUserData)
      }
    } yield ()
  }
}

object SignupNotifyService {

  def buildNotification(
    sender: String,
    newUser: NewUser,
    newUserData: NewUserData,
    nbRecentlyUsersMaxToKeep: Int = 5
  ): UserNotification = {

    val newRecentlyUsers = newUserData.newUserList
      .sortBy(_.createdAt)(Ordering[LocalDateTime].reverse)
      .take(nbRecentlyUsersMaxToKeep)

    val message = {
      val welcome = s"Hi ${newUser.name}, welcome to komoot"

      newRecentlyUsers.map(_.name) match {
        case Nil => s"$welcome."
        case head :: tail if tail.isEmpty => s"$welcome, $head also joined recently."
        case head :: tail => s"$welcome, ${tail.mkString(", ")} and $head also joined recently."
      }
    }

    UserNotification(sender, newUser.id, message, newRecentlyUsers.map(_.id))
  }
}
