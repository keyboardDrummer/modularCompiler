package core.parsers

import org.scalatest.FunSuite
import strings.StringReader
import editorParsers.EditorParserWriter
import strings.CommonParserWriter

trait AssociativityTest extends FunSuite with CommonParserWriter with EditorParserWriter {

  test("binary operators are right associative by default") {
    lazy val expr: EditorParser[Any] = new EditorLazy(expr) ~< "-" ~ expr | wholeNumber
    val input = "1-2-3"
    val result = expr.parseWholeInput(new StringReader(input))
    assert(result.successful)
    assertResult(("1",("2","3")))(result.get)
  }

  test("binary operators can be made left associative") {
    lazy val expr: EditorParser[Any] = wholeNumber.addAlternative[Any]((before, after) => after ~< "-" ~ before)
    val input = "1-2-3"
    val result = expr.parseWholeInput(new StringReader(input))
    assert(result.successful)
    assertResult((("1","2"),"3"))(result.get)
  }

  test("binary operators can be explicitly right associative") {
    lazy val expr: EditorParserExtensions[Any] = wholeNumber.addAlternative[Any]((before, after) => before ~< "-" ~ after)
    val input = "1-2-3"
    val result = expr.parseWholeInput(new StringReader(input))
    assert(result.successful)
    assertResult(("1",("2","3")))(result.get)
  }

  test("if-then-else can be made right-associative") {
    lazy val expr = wholeNumber
    val stmt: EditorParser[Any] = expr.
      addAlternative[Any]((before, after) => "if" ~> expr ~ "then" ~ after ~ "else" ~ before).
      addAlternative[Any]((before, after) => "if" ~> expr ~ "then" ~ after)
    val input = "if1thenif2then3else4"
    val result = stmt.parseWholeInput(new StringReader(input))
    assert(result.successful)
    val nestedIf = (((("2", "then"), "3"), "else"), "4")
    assertResult((("1","then"),nestedIf))(result.get)

    val input2 = "if1thenif2then3else4else5"
    val result2 = stmt.parseWholeInput(new StringReader(input2))
    assert(result2.successful)

    val input3 = "if1thenif2then3"
    val result3 = stmt.parseWholeInput(new StringReader(input3))
    assert(result3.successful)
  }
}