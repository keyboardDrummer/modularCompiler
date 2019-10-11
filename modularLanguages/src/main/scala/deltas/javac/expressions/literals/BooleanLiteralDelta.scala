package deltas.javac.expressions.literals

import core.deltas._
import core.deltas.grammars.LanguageGrammars
import core.deltas.path.NodePath
import core.language.node.{Node, NodeField, NodeShape}
import core.language.{Compilation, Language}
import core.smarts.ConstraintBuilder
import core.smarts.scopes.objects.Scope
import core.smarts.types.objects.Type
import deltas.expression.{ExpressionDelta, ExpressionInstance}
import deltas.javac.types.BooleanTypeDelta

object BooleanLiteralDelta extends DeltaWithGrammar with ExpressionInstance {
  val shape = Shape

  override def dependencies: Set[Contract] = Set(ExpressionDelta)

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    import grammars._
    val booleanLiteral = ("true".setValue(true) | "false".setValue(false)).as(Value).asLabelledNode(Shape)
    val expressionGrammar = find(ExpressionDelta.FirstPrecedenceGrammar)
    expressionGrammar.addAlternative(booleanLiteral)
  }

  def literal(value: Boolean) = new Node(Shape, Value -> value)

  def getValue(literal: Node) = literal(Value).asInstanceOf[Boolean]

  object Shape extends NodeShape

  object Value extends NodeField

  override def description: String = "Adds the boolean literals 'true' and 'false'"

  override def constraints(compilation: Compilation, builder: ConstraintBuilder, expression: NodePath, _type: Type, parentScope: Scope): Unit = {
    builder.typesAreEqual(_type, BooleanTypeDelta.constraintType)
  }
}