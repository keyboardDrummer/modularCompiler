package miksilo.modularLanguages.core.bigrammar

import miksilo.modularLanguages.core.bigrammar.grammars.{BiChoice, BiSequence, ManyHorizontal, ManyVertical, SequenceBijective, ValueGrammar}
import miksilo.editorParser.document.BlankLine
import miksilo.modularLanguages.core.node.Node

object BiGrammarSequenceCombinatorsExtension {

  def someMap(grammar: BiGrammar): BiGrammar = {
    grammar.mapSome[(Any, Seq[Any]), Seq[Any]](
      t => Seq(t._1) ++ t._2,
      seq => if (seq.nonEmpty) Some((seq.head, seq.tail)) else None)
  }
}

trait BiGrammarSequenceCombinatorsExtension extends BiGrammarWriter {
  import BiGrammarSequenceCombinatorsExtension._

  def grammar: BiGrammar
  def sequence(other: BiGrammar, bijective: SequenceBijective, horizontal: Boolean): BiGrammar

  def ~(other: BiGrammar): BiGrammar = sequence(other, BiSequence.identity, true)
  def %(other: BiGrammar): BiGrammar = sequence(other, BiSequence.identity, false)

  def ~<(right: BiGrammar): BiGrammar = sequence(right, BiSequence.ignoreRight, true)

  def ~>(right: BiGrammar): BiGrammar = sequence(right, BiSequence.ignoreLeft, true)

  def %>(bottom: BiGrammar): BiGrammar = sequence(bottom, BiSequence.ignoreLeft, false)

  def %<(bottom: BiGrammar): BiGrammar = sequence(bottom, BiSequence.ignoreRight, false)

  def many: ManyHorizontal
  def manyVertical: ManyVertical

  implicit def addSequenceMethods(grammar: BiGrammar): BiGrammarSequenceCombinatorsExtension

  def ~~<(right: BiGrammar): BiGrammar = this ~< leftRight(printSpace, right, BiSequence.ignoreLeft)

  def manySeparated(separator: BiGrammar): BiGrammar =
    new BiChoice(someSeparated(separator), ValueGrammar(Seq.empty[Any]), true)

  def ~~(right: BiGrammar): BiGrammar = {
    leftRight(grammar, printSpace, BiSequence.ignoreRight) ~ right
  }

  def someSeparatedVertical(separator: BiGrammar): BiGrammar =
    someMap(this % (separator %> grammar).manyVertical)

  def manySeparatedVertical(separator: BiGrammar): BiGrammar = new BiChoice(someSeparatedVertical(separator),
    ValueGrammar(Seq.empty[Node]), true)

  def some: BiGrammar = someMap(grammar ~ (grammar.*))
  def someSeparated(separator: BiGrammar): BiGrammar = someMap(this ~ ((separator ~> grammar).*))

  def inParenthesis: BiGrammar = ("(": BiGrammar) ~> grammar ~< ")"
  def inBraces: BiGrammar = ("{": BiGrammar) ~> grammar ~< "}"

  def ~~>(right: BiGrammar): BiGrammar = leftRight(grammar, printSpace, BiSequence.ignoreRight) ~> right

  def * : ManyHorizontal = many

  def %%(bottom: BiGrammar): BiGrammar = {
    (this %< BlankLine) % bottom
  }

  def spacedOption: BiGrammar = (printSpace ~> grammar).option
  def toParameterList: BiGrammar = grammar.manySeparated(stringToGrammar(",") ~ printSpace).inParenthesis
}

