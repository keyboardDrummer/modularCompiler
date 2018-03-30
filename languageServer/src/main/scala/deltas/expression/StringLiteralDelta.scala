package deltas.expression

import core.bigrammar.grammars.StringLiteral
import core.deltas.Contract
import core.deltas.grammars.LanguageGrammars
import core.deltas.path.NodePath
import core.language.node.{Node, NodeField, NodeShape}
import core.language.{Compilation, Language}
import core.smarts.ConstraintBuilder
import core.smarts.scopes.objects.Scope
import core.smarts.types.objects.{PrimitiveType, Type}
import deltas.javac.expressions.{ExpressionInstance, ExpressionSkeleton}

object StringLiteralDelta extends ExpressionInstance {

  override def description: String = "Adds the usage of string literals."

  val shape = Shape

  override def dependencies: Set[Contract] = Set(ExpressionSkeleton)

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    import grammars._
    val inner = StringLiteral
    val grammar = inner.as(Value).asLabelledNode(Shape)
    find(ExpressionSkeleton.ExpressionGrammar).addAlternative(grammar)
  }

  def literal(value: String) = new Node(Shape, Value -> value)

  def getValue(literal: Node): String = literal(Value).asInstanceOf[String]

  override def getType(expression: NodePath, compilation: Compilation): Node = ???

  object Shape extends NodeShape

  object Value extends NodeField

  override def constraints(compilation: Compilation, builder: ConstraintBuilder, expression: NodePath, _type: Type, parentScope: Scope): Unit = {
    builder.typesAreEqual(_type, PrimitiveType("String"))
  }
}