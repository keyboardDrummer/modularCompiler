package deltas.javac.expressions.literals

import core.deltas._
import core.deltas.grammars.LanguageGrammars
import core.deltas.node.{Node, NodeClass, NodeField}
import core.deltas.path.Path
import deltas.bytecode.constants.IntegerInfoConstant
import deltas.bytecode.coreInstructions.integers.{LoadConstantDelta, SmallIntegerConstantDelta}
import deltas.bytecode.types.IntTypeC
import deltas.javac.expressions.{ExpressionInstance, ExpressionSkeleton}

object IntLiteralDelta extends ExpressionInstance {
  val key = Clazz

  override def dependencies: Set[Contract] = Set(ExpressionSkeleton, SmallIntegerConstantDelta)

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    import grammars._
    val inner = number ^^(number => Integer.parseInt(number.asInstanceOf[String]), i => Some(i))
    val parseNumber = inner.as(Value).asLabelledNode(Clazz)
    find(ExpressionSkeleton.ExpressionGrammar).addOption(parseNumber)
  }

  def literal(value: Int) = new Node(Clazz, Value -> value)

  override def toByteCode(literal: Path, compilation: Compilation): Seq[Node] = {
    val value: Int = getValue(literal)
    if (-1 <= value && value <= 5) {
      val node = literal.current.shallowClone
      node.data.remove(Value)
      node.replaceWith(SmallIntegerConstantDelta.integerConstant(value), keepData = true)
      Seq(node) //TODO dit mooier maken. Maak de nieuwe node gewoon en en schuif deze over de oude node.
    }
    else
    {
      Seq(LoadConstantDelta.integerConstant(IntegerInfoConstant.construct(value)))
    }
  }

  def getValue(literal: Node): Int = literal(Value).asInstanceOf[Int]

  override def getType(expression: Path, compilation: Compilation): Node = IntTypeC.intType

  object Clazz extends NodeClass

  object Value extends NodeField

  override def description: String = "Adds the usage of int literals."
}