package io.komoot

import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

trait AppHelpers
    extends AnyWordSpec with OneInstancePerTest with Matchers with IdiomaticMockito with ArgumentMatchersSugar
    with ModelHelpers
