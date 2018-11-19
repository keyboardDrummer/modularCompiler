package core.bigrammar

import core.bigrammar.grammars._

object DefaultBiGrammarWriter extends DefaultBiGrammarWriter

trait DefaultBiGrammarWriter extends BiGrammarWriter {

  implicit def stringToAstGrammar(value: String): BiGrammarExtension =
    new BiGrammarExtension(BiGrammarWriter.stringToGrammar(value))
  implicit def grammarToAstGrammar(value: BiGrammar): BiGrammarExtension = new BiGrammarExtension(value)

  class BiGrammarExtension(val grammar: BiGrammar) extends BiGrammarSequenceCombinatorsExtension {

    override def topBottom(bottom: BiGrammar, bijective: SequenceBijective): TopBottom =
      new TopBottom(grammar, bottom, bijective)

    override def leftRight(other: BiGrammar, bijective: SequenceBijective): LeftRight =
      new LeftRight(grammar, other, bijective)

    override def many: ManyHorizontal = new ManyHorizontal(grammar)

    override def manyVertical: ManyVertical = new ManyVertical(grammar)

    override implicit def addSequenceMethods(grammar: BiGrammar): BiGrammarSequenceCombinatorsExtension = new BiGrammarExtension(grammar)
  }
}