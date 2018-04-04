package core.gridParser

import core.gridParser.grids.GridFromString
import org.scalatest.FunSuite

import scala.util.parsing.combinator.JavaTokenParsers

class YamlTest extends FunSuite with RegexGridParsers with JavaTokenParsers {

  trait YamlExpression
  case class Object(members: Map[String, YamlExpression]) extends YamlExpression
  case class Array(elements: Seq[YamlExpression]) extends YamlExpression
  case class Number(value: Int) extends YamlExpression

  lazy val parseValue: GridParser[Char, YamlExpression] = parseObject.name("object") | parseArray | parseNumber

  lazy val parseObject: GridParser[Char, YamlExpression] = {
    val key = FromLinearParser(ident <~ literal(":"))
    val member: GridParser[Char, (String, YamlExpression)] = key ~ parseValue | key % parseValue.indent(1)
    member.someVertical.map(values => Object(values.toMap))
  }

  lazy val parseArray: GridParser[Char, YamlExpression] = {
    val element = (FromLinearParser(literal("- ")) % Indent(2, canBeWider = false)) ~> parseValue
    element.someVertical.map(elements => Array(elements))
  }

  lazy val parseNumber: GridParser[Char, YamlExpression] =
    FromLinearParser(wholeNumber).map(n => Number(Integer.parseInt(n)))

  implicit def toGrid(value: String): GridFromString = GridFromString(value)

  test("number") {
    val program = "3"

    val result = parseNumber.parse(program)
    val expectation = ParseSuccess(Size(1, 1), Number(3))
    assertResult(expectation)(result)
  }

  test("inline member") {
    val key = FromLinearParser(ident <~ literal(":"))
    val member: GridParser[Char, (String, YamlExpression)] = key ~ parseNumber
    val program = "hallo: 3"
    val result = member.parse(program)
    assertResult(ParseSuccess(Size(8, 1), ("hallo", Number(3))))(result)
  }

  test("inline members") {
    val key = FromLinearParser(ident <~ literal(":"))
    val member: GridParser[Char, (String, YamlExpression)] = (key ~ parseNumber).name("member")

    val program =
      """minecraft: 2
        |cancelled: 3""".stripMargin
    val result = member.someVertical.parse(program)
    assertResult(ParseSuccess(Size(12, 2), List("minecraft" -> Number(2), "cancelled" -> Number(3))))(result)
  }

  test("object with 2 members") {
    val program =
      """minecraft: 2
        |cancelled: 3""".stripMargin

    val result = parseValue.parse(program)
    val expectation = ParseSuccess(Size(12, 2), Object(Map("minecraft" -> Number(2), "cancelled" -> Number(3))))
    assertResult(expectation)(result)
  }

  test("array") {
    val program =
      """- 2
        |- 3""".stripMargin

    val result = parseValue.parse(program)
    val expectation = ParseSuccess(Size(3, 2), Array(Seq(Number(2), Number(3))))
    assertResult(expectation)(result)
  }

  test("array object composite 2") {
    val program =
      """- x: 3
        |  y: 4
        |- 2""".stripMargin

    val result = parseValue.parse(program)
    val expectation = ParseSuccess(Size(6, 3), Array(Seq(Object(Map("x" -> Number(3), "y" -> Number(4))), Number(2))))
    assertResult(expectation)(result)
  }

  test("array object composite") {
    val program =
      """- 2
        |- x: 3
        |  y: 4""".stripMargin

    val result = parseValue.parse(program)
    val expectation = ParseSuccess(Size(6, 3), Array(Seq(Number(2), Object(Map("x" -> Number(3), "y" -> Number(4))))))
    assertResult(expectation)(result)
  }

  test("complex composite") {
    val program =
      """- 2
        |- x: 3
        |  y: a: 4
        |     b: 5
        |  z: - 2
        |     - 4
        |- 6
        |- q: - 7
        |     - 8
        |  r: 9""".stripMargin

    val result = parseValue.parse(program)
    val expectation = ParseSuccess(Size(9, 10),
      Array(Seq(
        Number(2),
          Object(Map("x" -> Number(3),
            "y" -> Object(Map("a" -> Number(4), "b" -> Number(5))),
            "z" -> Array(Seq(Number(2), Number(4))))),
        Number(6),
        Object(Map(
          "q" ->
            Array(Seq(Number(7), Number(8))),
          "r" -> Number(9))))))
    assertResult(expectation)(result)
  }
}
