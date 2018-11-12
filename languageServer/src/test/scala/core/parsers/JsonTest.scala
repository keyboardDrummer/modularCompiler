package core.parsers

import core.parsers.strings.StringReader
import org.scalatest.FunSuite

class JsonTest extends FunSuite with CommonParserWriter {

  private lazy val memberParser = stringLiteral ~< ":" ~ jsonParser
  private lazy val objectParser = "{" ~> memberParser.manySeparated(",") ~< "}"
  object UnknownExpression
  private lazy val jsonParser: Parser[Any] = WithDefault(stringLiteral | objectParser | wholeNumber, UnknownExpression)

  test("object with single member with number value") {
    val input = """{"person":3}"""
    val result = jsonParser.parseWhole(StringReader(input.toCharArray))
    val value = getSuccessValue(result)
    assertResult(List(("person","3")))(value)
  }

  test("object with single member with string value") {
    val input = """{"person":"remy"}"""
    val result = jsonParser.parseWhole(StringReader(input.toCharArray))
    val value = getSuccessValue(result)
    assertResult(List(("person","remy")))(value)
  }

  test("garbage after number") {
    val input = """3blaa"""
    assertInputGivesPartialFailureExpectation(input, "3")
  }

  test("nothing as input") {
    val input = ""
    assertInputGivesPartialFailureExpectation(input, UnknownExpression)
  }

  test("object start with nothing else") {
    val input = """{"""
    assertInputGivesPartialFailureExpectation(input, List.empty)
  }

  test("object member with only the key") {
    val input = """{"person""""
    val expectation = List(("""person""", UnknownExpression))
    assertInputGivesPartialFailureExpectation(input, expectation)
  }

  test("object member with no expression") {
    val input = """{"person":"""
    val expectation = List(("person", UnknownExpression))
    assertInputGivesPartialFailureExpectation(input, expectation)
  }

  test("object member with only an unfinished key") {
    val input = """{"person"""
    val expectation = List(("person", UnknownExpression))
    assertInputGivesPartialFailureExpectation(input, expectation)
  }

  test("object member with an unfinished value") {
    val input = """{"person":"remy"""
    assertInputGivesPartialFailureExpectation(input, List(("person", "remy")))
  }

  test("object with a single member and comma") {
    val input = """{"person":3,"""
    val expectation = List(("person", "3"))
    assertInputGivesPartialFailureExpectation(input, expectation)
  }

  test("object with a single member and half second member") {
    val input = """{"person":3,"second""""
    val expectation = List(("person", "3"), ("second", UnknownExpression))
    assertInputGivesPartialFailureExpectation(input, expectation)
  }

  private def assertInputGivesPartialFailureExpectation(input: String, expectation: Any) = {
    val result = jsonParser.parseWhole(StringReader(input.toCharArray))
    val failure: ParseFailure[Any] = getFailure(result)
    assert(failure.partialResult.nonEmpty)
    assertResult(expectation)(failure.partialResult.get)
  }

  private def getFailure(result: ParseResult[Any]): ParseFailure[Any] = {
    assert(result.isInstanceOf[ParseFailure[_]])
    result.asInstanceOf[ParseFailure[Any]]
  }

  private def getSuccessValue(result: ParseResult[Any]) = {
    assert(result.isInstanceOf[ParseSuccess[_]])
    result.asInstanceOf[ParseSuccess[Any]].result
  }
}


