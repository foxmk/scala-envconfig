package com.github.foxmk.envconfig

import com.github.foxmk.envconfig.ConfigParser.{EnvVariableNotFound, UnmarkedFields}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.util.{Failure, Success}

case class SimpleConfig(@env("STRING") string: String)

case class InvalidConfig(string: String)

@envprefix("PREFIX_")
case class PrefixConfig(@env("STRING") string: String)

class ConfigParserTest extends FlatSpec {

  "ConfigParser" should "take values from given environment" in {
    val env = Map("STRING" -> "foo", "BOOL" -> "true")
    ConfigParser.parse[SimpleConfig](env) shouldBe Success(SimpleConfig("foo"))
  }

  it should "return error if env variable not found" in {
    val env = Map.empty[String, String]
    ConfigParser.parse[SimpleConfig](env) shouldBe Failure(EnvVariableNotFound("STRING"))
  }

  it should "take values with prefix" in {
    val env = Map("PREFIX_STRING" -> "bar", "PREFIX_BOOL" -> "false")
    ConfigParser.parse[PrefixConfig](env) shouldBe Success(PrefixConfig("bar"))
  }
}
