
```scala
package io.komoot.server.routes.specs

import cats.effect.IO
import org.http4s.{Request, Response}

trait HttpNewUserSignupRouteSpec {
```


____
### [NEW-USER-SIGNUP](#new-user-signup)
<p>
  Simulate the 'ARN:SNS' which sends a notification to this client.
</p>

**204 - POST** `~/api/new-user-signup`

**PAYLOAD**

`application/json`

| Fields                    | Type    | Description
|---------------------------|:-------:|------------
| name                      | String  | Name of the new signup user
| id                        | Long    | Id of the new signup user
| created_at                | String  | Date of the signup

```json
{
  "name": "Marcus",
  "id": 1589278470,
  "created_at": "2020-05-12T16:11:54.000"
}
```


```scala
  def notifyNewUserSignup(req: Request[IO]): IO[Response[IO]]
```


____
### [AWS-SNS-NEW-USER-SIGNUP](#aws-sns-new-user-signup)
<p>
  The API where the 'ARN:SNS' sends the notifications to this client.
</p>

**204 - POST** `~/api/aws-sns/new-user-signup`

**PAYLOAD**

`application/json`

| Fields                    | Type    | Description
|---------------------------|:-------:|------------
| Message                   | String  | Contains the message sends by the ARN:SNS
| SubscribeURL              | URL     | For the very first message, the client needs to confirm the subscription by calling this endpoint

```json
{
  "Message": "{\"created_at\": \"2020-05-12T16:11:54.000\", \"name\": \"Marcus\", \"id\": 1}",
  "SubscribeURL": null
}
```
____

```json
{
  "Message": "You have chosen to subscribe to the topic arn",
  "SubscribeURL": "https://sns.eu-west-3.amazonaws.com/?Action=ConfirmSubscription&TopicArn=arn:aw"
}
```


```scala
  def notifyNewUserSignupFromAwsSNS(req: Request[IO]): IO[Response[IO]]
}

```




[HttpStatusRouteSpec.scala]: HttpStatusRouteSpec.scala.md
[HttpNewUserSignupRouteSpec.scala]: HttpNewUserSignupRouteSpec.scala.md