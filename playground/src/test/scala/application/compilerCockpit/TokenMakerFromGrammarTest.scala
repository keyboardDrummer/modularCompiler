package application.compilerCockpit

import core.bigrammar.BiGrammarToParser
import deltas.javac.JavaLanguage
import deltas.javac.trivia.{JavaStyleBlockCommentsDelta, StoreTriviaDelta, TriviaInsideNode}
import org.fife.ui.rsyntaxtextarea.TokenTypes
import org.scalatest.FunSuite
import util.TestLanguageBuilder

import scala.util.parsing.input.CharArrayReader

class TokenMakerFromGrammarTest extends FunSuite {

  private val language = TestLanguageBuilder.build(Seq(TriviaInsideNode, StoreTriviaDelta, JavaStyleBlockCommentsDelta) ++ JavaLanguage.javaCompilerDeltas)

  test("fibonacci") {
    val grammar = language.grammars.root
    val tokenMaker = new TokenMakerFromGrammar(grammar)

    val text =
      """class Foo
        |{
        |    /* hello */
        |    public static int bar(int index)
        |    {
        |        return index < /* here comes two */ 2 ? 1 : 3;
        |    }
        |}""".stripMargin

    val charArrayReader = new CharArrayReader(text.toString.toCharArray)
    val resultOption: BiGrammarToParser.ParseResult[Seq[MyToken]] = tokenMaker.parser(charArrayReader)
    assert(resultOption.successful, resultOption.toString)
    val tokens = resultOption.get
    val space = MyToken(TokenTypes.WHITESPACE, " ")
    def spaces(number: Int) = MyToken(TokenTypes.WHITESPACE, new String(Array.fill(number)(' ')))
    def newLine(spaces: Int) = MyToken(TokenTypes.WHITESPACE, "\n" + new String(Array.fill(spaces)(' ')))
    def reserved(value: String) = MyToken(TokenTypes.RESERVED_WORD, value)
    def identifier(value: String) = MyToken(TokenTypes.IDENTIFIER, value)
    def delimiter(value: String) = MyToken(TokenTypes.SEPARATOR, value)
    def integer(value: Int) = MyToken(TokenTypes.LITERAL_NUMBER_DECIMAL_INT, value.toString)
    val expectedTokens = List(
      reserved("class"), space, identifier("Foo"), newLine(0),
      delimiter("{"), newLine(4),
      MyToken(TokenTypes.COMMENT_MULTILINE, "/* hello */"), newLine(4),
      reserved("public"), space, reserved("static"), space, reserved("int"), space, identifier("bar"),
        delimiter("("), reserved("int"), space, identifier("index"), delimiter(")"), newLine(4),
      delimiter("{"), newLine(8),
      reserved("return"), space, identifier("index"), space, delimiter("<") /* this should be operator */,
        space, MyToken(TokenTypes.COMMENT_MULTILINE, "/* here comes two */"), space, integer(2), space, delimiter("?"),
        space, integer(1), space, delimiter(":"), space, integer(3), delimiter(";"), newLine(4),
      delimiter("}"), newLine(0),
      delimiter("}")
    )

    assertResult(expectedTokens)(tokens)
  }

  test("failure") {
    val grammar = language.grammars.root
    val tokenMaker = new TokenMakerFromGrammar(grammar)

    val text = "^"

    val charArrayReader = new CharArrayReader(text.toString.toCharArray)
    val resultOption = tokenMaker.parser(charArrayReader)
    assert(resultOption.successful, resultOption.toString)
    val tokens = resultOption.get
    val expectedTokens = List(MyToken(TokenTypes.ERROR_CHAR, "^"))

    assertResult(expectedTokens)(tokens)
  }
}