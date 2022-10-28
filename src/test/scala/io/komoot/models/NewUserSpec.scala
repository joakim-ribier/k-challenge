package io.komoot.models

import io.circe.parser.parse
import io.circe.syntax.EncoderOps
import io.komoot.HelpersSpec

class NewUserSpec extends HelpersSpec {

  "NewUserSpec" must {

    "serialize object" in {
      marcus.newUser.asJson mustBe parse(
        """
          |{
          | "name" : "Marcus",
          | "id" : 1,
          | "created_at" : "2020-05-12T16:11:54"
          |}
          |""".stripMargin
      ).toOption.get
    }

    "deserialize object" in {
      parse(
        """
          |{
          | "name" : "Marcus",
          | "id" : 1,
          | "created_at" : "2020-05-12T16:11:54.000"
          |}
          |""".stripMargin
      ).toOption.get.as[NewUser].toOption.get mustBe marcus.newUser
    }
  }
}
