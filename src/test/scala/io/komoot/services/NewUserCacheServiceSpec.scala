package io.komoot.services

import java.time.LocalDateTime

import cats.effect.IO
import io.komoot.models.cache.NewUserData
import io.komoot.{AppHelpers, EnvHelpers}

import scalacache.caffeine.CaffeineCache

class NewUserCacheServiceSpec extends AppHelpers with EnvHelpers {

  "NewUserCacheService" must {

    "get the current data from the local cache" in {
      val service = new NewUserCacheService(CaffeineCache[IO, String, NewUserData].unsafeRunSync())

      service.getCache().unsafeRunSync() mustBe NewUserData.empty
    }

    "update the cache" in {
      val now = LocalDateTime.now()

      val service = new NewUserCacheService(CaffeineCache[IO, String, NewUserData].unsafeRunSync())

      service.getCache().unsafeRunSync() mustBe NewUserData.empty

      service.update(marcus.newUser, true, now).unsafeRunSync()
      service.update(lydia.newUser, true, now).unsafeRunSync()
      service.update(marcus.newUser, true, now).unsafeRunSync()

      service.getCache().unsafeRunSync().newUserList.map(_.id) must contain theSameElementsAs List(
        marcus.id,
        lydia.id
      )
    }
  }
}
