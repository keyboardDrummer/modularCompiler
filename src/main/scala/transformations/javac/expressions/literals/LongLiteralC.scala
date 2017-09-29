package transformations.javac.expressions.literals

import core.bigrammar.BiGrammar
import core.grammar.RegexG
import core.particles.grammars.GrammarCatalogue
import core.particles.node.{Key, Node}
import core.particles.path.Path
import core.particles.{Compilation, Contract, Language}
import transformations.bytecode.coreInstructions.integers.SmallIntegerConstantDelta
import transformations.bytecode.coreInstructions.longs.PushLongDelta
import transformations.javac.expressions.{ExpressionInstance, ExpressionSkeleton}
import transformations.bytecode.types.LongTypeC

object LongLiteralC extends ExpressionInstance {
  val key = LongLiteralKey

  override def dependencies: Set[Contract] = Set(ExpressionSkeleton, SmallIntegerConstantDelta)

  def parseLong(number: String) = java.lang.Long.parseLong(number.dropRight(1))

  override def transformGrammars(grammars: GrammarCatalogue, state: Language): Unit = {
    val longGrammar : BiGrammar = (new RegexG("""-?\d+l""".r) : BiGrammar) ^^
      (number => parseLong(number.asInstanceOf[String]), l => Some(s"${l}l")) asNode(LongLiteralKey, ValueKey)
    val expressionGrammar = grammars.find(ExpressionSkeleton.ExpressionGrammar)
    expressionGrammar.addOption(longGrammar)
  }

  def literal(value: Long) = new Node(LongLiteralKey, ValueKey -> value)

  override def toByteCode(literal: Path, state: Language): Seq[Node] = {
    Seq(PushLongDelta.constant(getValue(literal).toInt))
  }

  def getValue(literal: Node) = literal(ValueKey).asInstanceOf[Long]

  override def getType(expression: Path, state: Language): Node = LongTypeC.longType

  object LongLiteralKey extends Key

  object ValueKey extends Key

  override def description: String = "Adds the usage of long literals by putting an l after the number."
}
