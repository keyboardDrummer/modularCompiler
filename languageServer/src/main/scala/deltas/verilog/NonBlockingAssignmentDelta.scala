package deltas.verilog

import core.bigrammar.BiGrammar
import core.deltas.DeltaWithGrammar
import core.deltas.grammars.LanguageGrammars
import core.deltas.path.NodePath
import core.language.node._
import core.language.{Compilation, Language}
import core.smarts.ConstraintBuilder
import core.smarts.scopes.objects.Scope
import deltas.expressions.ExpressionDelta
import deltas.javac.classes.skeleton.HasConstraintsDelta
import deltas.statement.StatementDelta

object NonBlockingAssignmentDelta extends DeltaWithGrammar with HasConstraintsDelta { //TODO merge with AssignmentSkeleton
  object Shape extends NodeShape
  object Target extends NodeField
  object Value extends NodeField

  implicit class NonBlockingAssignment[T <: NodeLike](val node: T) extends NodeWrapper[T] {
    def target: String = node(Target).asInstanceOf[String]
    def value: T = node(Value).asInstanceOf[T]
  }

  def neww(target: String, value: Node): Node = Shape.create(Target -> target, Value -> value)

  override def transformGrammars(grammars: LanguageGrammars, language: Language): Unit = {
    import grammars._
    val expression = find(ExpressionDelta.FirstPrecedenceGrammar)
    val statement = find(StatementDelta.Grammar)
    val assignment: BiGrammar = identifier.as(Target) ~~ "<=" ~~ expression.as(Value) asNode Shape
    statement.addAlternative(assignment)
  }

  override def description: String = "Adds the non-blocking assignment operator <="

  override def shape: NodeShape = Shape

  override def collectConstraints(compilation: Compilation, builder: ConstraintBuilder, path: NodePath, parentScope: Scope): Unit = {
    val assignment: NonBlockingAssignment[NodePath] = path
    ExpressionDelta.constraints(compilation, builder, assignment.value, builder.typeVariable(), parentScope)
    builder.resolve(assignment.target, path.getLocation(Target), parentScope)
  }
}