# Komoot - Challenge 2022

## Synopsis

Http4s micro-service: to receive a message on a specific endpoint from `arn:sns` and transfer a new message to an external push notification service (Komoot API).

## TOC

* [Project](#project)
* [Stack](#stack)
* [Documentation](#documentation)
* [API](#api)
* [Test](#test)
* [Run](#run)
* [Deployment](#deployment)
* [Improvement](#improvement)

## Project

The service subscribes to an `arn:sns` (`application.conf#komoot.sns`) to receive messages on `application.conf#sns-endpoint-to-receive-notification` local endpoint. For the very first message, the `arn:sns` asks to its clients to confirm the subscription. After this, the client will receive all messages.

When it received a message (`new signup user`), it gets all current new signup users (stored in the local storage), builds a new notification message and posts the notification on the external notification service (`application.conf#komoot.api`).

For instance:

```json
#1 message received from the arn:sns
{
  "Message": {
    "name": "Marcus",
    "id": 100,
    "created_at": "2020-05-12T00:00:01.000"
  }
}
#2 message received from the arn:sns
{
  "Message": {
    "name": "Lydia",
    "id": 200,
    "created_at": "2020-05-13T00:00:01.000"
  }
}
```

To avoid a too big "welcome" message, the list of new users is limited with the last 5 users.

```json
# message built and pushed to the external notification service
{
  "sender": "application.conf#sender",
  "receiver": 200,
  "message": "Hi Lydia, welcome to komoot. Marcus also joined recently.",
  "recent_user_ids": [100]
}
```

There are 3 endpoints [see #API for more details](#api):

* GET ~/api/status => check if the app is correctly deployed
* GET ~/api/status/cache => check the current local storage (received messages)

* POST ~/api/new-user-signup => to simulate the message sent by the `arn:sns`

## Stack

The service is built with [http4s](https://http4s.org/), it comes with the IO and [cats](https://typelevel.org/cats/) library. It's the new way to do, more functionnal to avoid all side effects of the application and have only pure functions.

I used [circe](https://circe.github.io/circe/) library to work with Json data type. It's an awesome library to manipulate easily (serialize/deserialize) data.

To load configuration files, I used [pureconfig](https://github.com/pureconfig/pureconfig), there is no more simple way!

The project is built with the integration of the Github workflows [Scala CI](https://github.com/joakim-ribier/komoot-challenge/actions).

## Documentation

I used [Literator](https://github.com/laughedelic/literator) sbt plugin to generate the API specification.

```
addSbtPlugin("laughedelic" % "literator" % "0.8.0")
```

Then, you need to update the `io.komoot.server.routes.specs.*RouteSpecs` files and execute:

```
$ sbt generateDocs
```

[api](#api)

## API

HTTP STATUS ROUTES

* [GET - /api/status](resources/specs/HttpStatusRouteSpec.scala.md#status)
* [GET - /api/status/cache](resources/specs/HttpStatusRouteSpec.scala.md#cache)

HTTP NEW USER SIGNUP ROUTES

* [POST - /api/new-user-signup](resources/specs/HttpNewUserSignupRouteSpec.scala.md#new-user-signup)
* [POST - /api/aws-sns/new-user-signup](resources/specs/HttpNewUserSignupRouteSpec.scala.md#aws-sns-new-user-signup)

## Test

[![codecov](https://codecov.io/gh/joakim-ribier/komoot-challenge/branch/main/graph/badge.svg?token=0G7D8GY6HV)](https://codecov.io/gh/joakim-ribier/komoot-challenge)

The tests are in `src/test/scala/io/komoot` package.

To show the tests execution, go to the [Action on Scala CI - here](https://github.com/joakim-ribier/komoot-challenge/actions) click on the last build -> "build 1m 29s" -> "Run Tests". 

```
$ sbt:komoot-challenge> test
...
[info] Run completed in 5 seconds, 32 milliseconds.
[info] Total number of tests run: 30
[info] Suites: completed 10, aborted 0
[info] Tests: succeeded 30, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
```

## Run

Before running the app, update correctly the [application.conf](src/main/resources/application.conf).

```
# application.conf

sender = "{sender}"
sns-endpoint-to-receive-notification = "http://{host}:9700/api/aws-sns/new-user-signup"

aws {
  access-key-id = "{access-key-id}"
  secret-access-key = "{secret-access-key}"
}

komoot {
 sns = "{sns}"
 api = "{komoot-api}"
}

```

Run the server:

```
$sbt:komoot-challenge> run
[info] running io.komoot.Main 
[io-compute-blocker-5] INFO  io.komoot.Main - Starting application.
Config(joakim.ribier@gmail.com,http://35.181.62.254:9700/api/aws-sns/new-user-signup,5,AwsConfig({access-key-id},{secret-access-key}),KomootConfig(arn:aws:sns:eu-west-1:963797398573:challenge-backend-signups,https://notification-backend-challenge.main.komoot.net,false,eu-west-1),HttpConfig(0.0.0.0,9700)) 
[io-compute-blocker-5] INFO  i.k.a.SNSAwsService - Try to subscribe to SNS... 
[io-compute-blocker-5] INFO  i.k.a.SNSAwsService - Subscription to SNS Ok.
```

And curl it or directly with [Http Client (Go)](https://github.com/joakim-ribier/gttp):
```
$curl http://localhost:9700/api/status | jq
```
```json
{
  "name": "komoot-challenge",
  "commit": "57bcefe8b86ea375b377247245532350b2ffa10b",
  "version": "0.1.0-SNAPSHOT",
  "build": "2022-10-30 17:44:46.510+0100"
}
```

## Deployment

I deployed the app on AWS.

I created an EC2 instance on Linux distribution and installed Java 11 to run the service - redirect external 9700 port to the http4s local server.

Then, I generated the application with sbt to build the `*.zip` and uploaded it on the instance.

```
$ sbt dist
[info] Your package is ready in /home/joakim/Sources/external/komoot-challenge/target/universal/komoot-challenge-0.1.0-SNAPSHOT.zip
[success] Total time: 9 s, completed 30 oct. 2022 à 18:00:52

$ scp -i ~/.pem target/universal/komoot-challenge-0.1.0-SNAPSHOT.zip ec2-user@{ip}.compute.amazonaws.com:/tmp/
```

I decided to make it very simple, but for a more complex project, it is better to create a docker image and deploy it directly with terraform to automate the deployment.

## Improvement

Code:

* ~~Implement all missing tests and add coverage code on the project.~~

* ~~Update the project to use the `tagless final pattern` to remove at the maximum the all side effects. For a simple project like this, I think it is overkill to do that, the code review will be more simple for you!~~

Infra:

On Gitlab, it is possible to build a pipeline with several jobs: to execute test and to deploy automatically the `*.zip` to the EC2 instance and to run it.

Or for a big project, to automate the deployement with terraform and docker.
