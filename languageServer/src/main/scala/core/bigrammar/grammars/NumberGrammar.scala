package core.bigrammar.grammars

import core.bigrammar.BiGrammarToParser

object NumberGrammar extends PrintUsingToStringGrammar {
  override def getParser(keywords: scala.collection.Set[String]): BiGrammarToParser.Parser[Any] =
    BiGrammarToParser.wholeNumber
}