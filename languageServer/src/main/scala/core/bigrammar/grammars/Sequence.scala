package core.bigrammar.grammars

import core.bigrammar.BiGrammar
import core.bigrammar.printer.UndefinedDestructuringValue

object Sequence {

  def identity: SequenceBijective = SequenceBijective(packTuple, unpackTuple)

  private def packTuple: (Any, Any) => (Any, Any) = (a: Any, b: Any) => (a,b)
  private def unpackTuple: Any => Option[(Any, Any)] = {
    case UndefinedDestructuringValue => Some(UndefinedDestructuringValue, UndefinedDestructuringValue)
    case t: (Any, Any) => Some(t)
    case _ => None
  }

  def ignoreLeft = SequenceBijective((a: Any, b: Any) => b, x => Some(UndefinedDestructuringValue, x))
  def ignoreRight = SequenceBijective((a: Any, b: Any) => a, x => Some(x, UndefinedDestructuringValue))
}

case class SequenceBijective(construct: (Any, Any) => Any, destruct: Any => Option[(Any, Any)])

trait Sequence extends BiGrammar with Layout {
  def first: BiGrammar
  def first_=(value: BiGrammar): Unit

  def second: BiGrammar
  def second_=(value: BiGrammar): Unit

  def bijective: SequenceBijective

  override def children = Seq(first, second)

  override def containsParser(recursive: BiGrammar => Boolean): Boolean =
    recursive(first) || recursive(second)

  override protected def getLeftChildren(recursive: BiGrammar => Seq[BiGrammar]): Seq[BiGrammar] = {
    if (first.containsParser())
      recursive(first)
    else {
      recursive(second)
    }
  }
}
