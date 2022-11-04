package io.komoot.server.routes.specs

import org.http4s.Response

trait HttpStatusRouteSpec[F[_]] {

  /*
    ____
    ### [STATUS](#status)
    <p>
      Status

      Get the version of the deployed application.
    </p>

    **200 - GET** `~/api/status`

    **RESPONSE**

    `application/json`

    | Fields                    | Type    | Description
    |---------------------------|:-------:|------------
    | version                   | String  | Version (0.0.1)
    | build                     | String  | DateTime of the build
    | commit                    | String  | Last commit
    | name                      | String  | Name

    ```json
    {
      "name": "komoot-challenge",
      "commit": "57bcefe8b86ea375b377247245532350b2ffa10b",
      "version": "0.1.0-SNAPSHOT",
      "build": "2022-10-30 15:18:49.418+0100"
    }
    ```
   */
  def status(): F[Response[F]]

  /*
    ____
    ### [CACHE](#scache)
    <p>
      Cache

      Display the state of the actual data (local cache). The cache is clean every 2 hours.
    </p>

    **200 - GET** `~/api/status/cache`

    **RESPONSE**

    `application/json`

    | Fields                    | Type           | Description
    |---------------------------|:--------------:|------------
    | newUser                   | NewUser        | Payload received from the 'arn:sns'
    | pushNotification          | Boolean        | 'True' if the message has been sent to the push notification service (Komoot)
    | receivedMessageAt         | LocalDateTime  | LocalDateTime of the received message from 'arn:sns'

    ```json
    [
      {
        "newUser": {
          "name": "Marcus",
          "id": 1589278470,
          "created_at": "2020-05-12T16:11:54"
        },
        "pushNotification": true,
        "receivedMessageAt": "2022-10-30T15:19:16.855552"
      },
      {
        "newUser": {
          "name": "Lydia",
          "id": 1689278470,
          "created_at": "2020-05-12T16:11:54"
        },
        "pushNotification": true,
        "receivedMessageAt": "2022-10-30T15:19:58.861612"
      }
    ]
    ```
   */
  def cache(): F[Response[F]]
}
