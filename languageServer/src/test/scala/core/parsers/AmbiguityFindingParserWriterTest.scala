package core.parsers

import org.scalatest.FunSuite
import editorParsers.AmbiguityFindingParserWriter
import editorParsers.LeftRecursiveCorrectingParserWriter
import _root_.core.parsers.editorParsers.NeverStop

class AmbiguityFindingParserWriterTest extends FunSuite
  with AmbiguityFindingParserWriter
  with CommonStringReaderParser with LeftRecursiveCorrectingParserWriter {

  test("detects trivial ambiguity") {
    val ambiguis = Literal("a") | Literal("a")
    val input = "a"
    assertThrows[Exception](ambiguis.getWholeInputParser.parse(new StringReader(input), NeverStop))
  }
}