package io.komoot.models

import io.circe.parser.parse
import io.circe.syntax.EncoderOps
import io.komoot.AppHelpers

class UserNotificationSpec extends AppHelpers {

  "UserNotificationSpec" must {

    "serialize object" in {
      userNotification.asJson mustBe parse(
        """
          |{
          | "sender" : "test@komoot.io",
          | "receiver" : 1,
          | "message" : "Hi Marcus, welcome to komoot, Lydia also joined recently.",
          | "recent_user_ids" : [2]
          |}
          |""".stripMargin
      ).toOption.get
    }

    "deserialize object" in {
      parse(
        """
          |{
          | "sender" : "test@komoot.io",
          | "receiver" : 1,
          | "message" : "Hi Marcus, welcome to komoot, Lydia also joined recently.",
          | "recent_user_ids" : [2]
          |}
          |""".stripMargin
      ).toOption.get.as[UserNotification].toOption.get mustBe userNotification
    }
  }
}
