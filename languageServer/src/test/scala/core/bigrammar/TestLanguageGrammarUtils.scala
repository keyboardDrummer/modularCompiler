package core.bigrammar

import core.bigrammar.TestGrammarUtils.parseAndPrintSame
import core.bigrammar.printer.BiGrammarToPrinter
import core.deltas.grammars.LanguageGrammars
import core.deltas.{Contract, Delta, DeltaWithGrammar}
import core.language.Language
import core.language.node.GrammarKey
import core.parsers.StringReader
import deltas.ClearPhases
import deltas.javac.JavaLanguage
import org.scalatest.FunSuite
import util.TestLanguageBuilder


case class StringKey(value: String) extends GrammarKey

object TestLanguageGrammarUtils extends TestLanguageGrammarUtils(JavaLanguage.javaCompilerDeltas)

object TestGrammarUtils extends FunSuite {

  def parseAndPrintSame(example: String, expectedOption: Option[Any] = None, grammarDocument: BiGrammar): Unit = {
    val documentResult: String = parseAndPrint(example, expectedOption, grammarDocument)
    assertResult(example)(documentResult)
  }

  def parseAndPrint(example: String, expectedOption: Option[Any], grammarDocument: BiGrammar): String = {
    val parseResult = parse(example, grammarDocument)
    assert(parseResult.successful, parseResult.toString)

    val result = parseResult.get

    expectedOption.foreach(expected => assertResult(expected)(result))

    print(result, grammarDocument)
  }

  def print(result: Any, grammarDocument: BiGrammar): String = {
    BiGrammarToPrinter.toDocument(result, grammarDocument).renderString()
  }

  def parse(example: String, grammarDocument: BiGrammar): BiGrammarToParser.ParseResult[Any] = {
    val parser = BiGrammarToParser.toParser(grammarDocument)
    parser.parseWhole(new StringReader(example))
  }
}

case class TestLanguageGrammarUtils(deltas: Seq[Delta]) extends FunSuite {

  def compareInputWithPrint(input: String, expected: Option[Any] = None, grammarTransformer: GrammarKey = null): Unit = {
    val grammar = getGrammarUsingTransformer(grammarTransformer)
    parseAndPrintSame(input, expected, grammar)
  }

  def getPrintResult(value: Any, grammarTransformer: GrammarKey = null): String = {
    val document = getGrammarUsingTransformer(grammarTransformer)
    BiGrammarToPrinter.toDocument(value, document).renderString()
  }

  def getGrammarUsingTransformer(grammarTransformer: GrammarKey = null): BiGrammar = {
    TestLanguageBuilder.buildWithParser(getDeltas(grammarTransformer)).language.grammars.root
  }

  def parse(input: String, grammarTransformer: GrammarKey = null): Any = {
    val compiler = TestLanguageBuilder.buildWithParser(Seq(ClearPhases) ++ getDeltas(grammarTransformer))
    val result = compiler.compile(input).program
    result.startOfUri = None
    result
  }

  def getDeltas(key: GrammarKey): Seq[Delta] = {
    Seq(new SelectorTransformation(key)) ++ deltas
  }

  class SelectorTransformation(key: GrammarKey) extends DeltaWithGrammar {
    override def transformGrammars(grammars: LanguageGrammars, language: Language): Unit = {
      if (key != null)
        grammars.root.inner = grammars.find(key)
    }

    override def dependencies: Set[Contract] = Set.empty

    override def description: String = "Sets the program grammar to a specific grammar from the grammar catalogue."
  }

}
