package io.komoot

import io.circe.Json
import org.scalatest.matchers.{MatchResult, Matcher}

object MatcherHelpers {

  object JsonMatchers {

    def contains(at: String, value: String): Matcher[Json] = new Matcher[Json] {
      override def apply(json: Json): MatchResult = {
        val result = json.\\(at).flatMap(_.asString).contains(value)
        MatchResult(
          result,
          s"$json does not contain $value at $at.",
          s"$json contains $value at $at."
        )
      }
    }
  }
}
