package io.komoot.services

import java.time.LocalDateTime

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import io.komoot.config.{Config, KomootConfig}
import io.komoot.models.cache.NewUserData
import io.komoot.models.{NewUser, UserNotification}
import io.komoot.services.http.PushNotificationHttpService
import io.komoot.{EnvHelpers, AppHelpers}

class SignupNotifyServiceSpec extends AppHelpers with EnvHelpers {

  "SignupNotifyService" when {

    val config = Config("test@komoot.io", "sns", 5, null, null, null)

    "notify" must {

      "do nothing if user already exists" in {
        val newUserCacheService = mock[NewUserCacheService]
        newUserCacheService.getCache().returns(IO.pure(NewUserData(List(marcus))))

        val service = new SignupNotifyService(config, newUserCacheService, null)

        val now = LocalDateTime.now()
        service.notify(marcus.newUser, now).unsafeRunSync()

        newUserCacheService.getCache().wasCalled(once)
        logger.info(s"Do not notify user ${marcus.name} again, he already exists in our platform.").wasCalled(once)
      }

      "push notification for new signup user" in {
        val newUserCacheService = mock[NewUserCacheService]
        newUserCacheService.getCache().returns(IO.pure(NewUserData(List(lydia))))
        newUserCacheService.update(*[NewUser], *[Boolean], *[LocalDateTime]).returns(IO.unit)

        val pushNotificationHttpService = mock[PushNotificationHttpService]
        pushNotificationHttpService.push(*[UserNotification]).returns(true.pure[IO])

        val service = new SignupNotifyService(
          config.copy(komoot = KomootConfig("", "", true, "")),
          newUserCacheService,
          pushNotificationHttpService
        )

        val now = LocalDateTime.now()
        service.notify(marcus.newUser, now).unsafeRunSync()

        newUserCacheService.getCache().wasCalled(once)
        logger.info(s"Notify user ${marcus.name}, that he is welcome to our platform.").wasCalled(once)
        val userNotification = SignupNotifyService.buildNotification(
          "test@komoot.io",
          marcus.newUser,
          NewUserData(List(lydia))
        )
        pushNotificationHttpService.push(userNotification).wasCalled(once)
        logger.info(s"The notification has been sent.\n\r$userNotification").wasCalled(once)
        newUserCacheService.update(*[NewUser], true, now).wasCalled(once)
      }
    }

    "build message" must {

      "build a simple welcome message if there is no recently signup users" in {
        val result = SignupNotifyService.buildNotification("test@komoot.io", marcus.newUser, NewUserData.empty)

        result mustBe UserNotification("test@komoot.io", 1, "Hi Marcus, welcome to komoot.", List.empty[Long])
      }

      "build welcome message if there is only one signup user" in {
        val result = SignupNotifyService.buildNotification("test@komoot.io", marcus.newUser, NewUserData(List(lydia)))

        result mustBe UserNotification(
          "test@komoot.io",
          1,
          "Hi Marcus, welcome to komoot, Lydia also joined recently.",
          List(2)
        )
      }

      "build welcome message if there is two signup users" in {
        val result =
          SignupNotifyService.buildNotification("test@komoot.io", marcus.newUser, NewUserData(List(lydia, lucas)))

        result mustBe UserNotification(
          "test@komoot.io",
          1,
          "Hi Marcus, welcome to komoot, Lydia and Lucas also joined recently.",
          List(3, 2)
        )
      }

      "build welcome message with nb recently max users" in {
        val result = SignupNotifyService.buildNotification(
          "test@komoot.io",
          marcus.newUser,
          NewUserData(List(lydia, lucas, patrick)),
          nbRecentlyUsersMaxToKeep = 1
        )

        result mustBe UserNotification(
          "test@komoot.io",
          1,
          "Hi Marcus, welcome to komoot, Patrick also joined recently.",
          List(patrick.id)
        )
      }

      "build welcome message if there is more than two signup users" in {
        val result =
          SignupNotifyService.buildNotification(
            "test@komoot.io",
            marcus.newUser,
            NewUserData(List(lydia, lucas, patrick))
          )

        result mustBe UserNotification(
          "test@komoot.io",
          1,
          "Hi Marcus, welcome to komoot, Lucas, Lydia and Patrick also joined recently.",
          List(4, 3, 2)
        )
      }
    }
  }
}
