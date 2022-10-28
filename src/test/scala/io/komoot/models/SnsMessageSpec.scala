package io.komoot.models

import io.circe.parser.parse
import io.komoot.HelpersSpec
import io.komoot.models.aws.SnsMessage

class SnsMessageSpec extends HelpersSpec {

  "SnsMessageSpec" must {

    "deserialize object with 'new user' message" in {

      parse(
        """
          |{
          |  "Message" : "{\"created_at\": \"2020-05-12T16:11:54.000\", \"name\": \"Marcus\", \"id\": 1}"
          |}
          |""".stripMargin
      ).toOption.get.as[SnsMessage].toOption.get mustBe SnsMessage(maybeNewUser = Some(marcus.newUser), None)
    }

    "deserialize object with 'subscribe url' message" in {
      parse(
        """
          |{
          |  "Message" : "You have chosen to subscribe to the topic arn:aws...",
          |  "SubscribeURL" : "https://sns.eu-west-3.amazonaws.com/?Action=ConfirmSubscription&T"
          |}
          |""".stripMargin
      ).toOption.get.as[SnsMessage].toOption.get mustBe SnsMessage(
        maybeNewUser = None,
        Some("https://sns.eu-west-3.amazonaws.com/?Action=ConfirmSubscription&T")
      )
    }
  }
}
