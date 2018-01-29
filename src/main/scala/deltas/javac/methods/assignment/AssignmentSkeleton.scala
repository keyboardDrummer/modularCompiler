package deltas.javac.methods.assignment

import core.bigrammar.grammars.BiFailure
import core.deltas._
import core.deltas.grammars.LanguageGrammars
import core.deltas.node._
import core.deltas.path.NodePath
import core.language.Language
import core.nabl.ConstraintBuilder
import core.nabl.scopes.objects.Scope
import core.nabl.types.objects.Type
import deltas.bytecode.coreInstructions.integers.StoreIntegerDelta
import deltas.bytecode.coreInstructions.objects.StoreAddressDelta
import deltas.bytecode.coreInstructions.{Duplicate2InstructionDelta, DuplicateInstructionDelta}
import deltas.javac.expressions.{ExpressionInstance, ExpressionSkeleton}
import deltas.javac.methods.MethodDelta
import deltas.bytecode.types.TypeSkeleton

object AssignmentSkeleton extends ExpressionInstance with WithLanguageRegistry {

  def getAssignmentTarget[T <: NodeLike](assignment: T): T = assignment(AssignmentTarget).asInstanceOf[T]

  def getAssignmentValue[T <: NodeLike](assignment: T): T = assignment(AssignmentValue).asInstanceOf[T]

  override def dependencies: Set[Contract] = Set(MethodDelta, StoreAddressDelta, StoreIntegerDelta, AssignmentPrecedence)

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    import grammars._
    val targetGrammar = create(AssignmentTargetGrammar, BiFailure())
    val expressionGrammar = find(ExpressionSkeleton.ExpressionGrammar) //TODO shouldn't this use AssignmentPrecedence?
    val assignmentGrammar = targetGrammar.as(AssignmentTarget) ~~< "=" ~~ expressionGrammar.as(AssignmentValue) asNode AssignmentKey
    expressionGrammar.addOption(assignmentGrammar)
  }

  object AssignmentTargetGrammar extends GrammarKey

  def assignment(target: Node, value: Node) = new Node(AssignmentKey, AssignmentTarget -> target, AssignmentValue -> value)

  object AssignmentKey extends NodeShape

  object AssignmentTarget extends NodeField

  object AssignmentValue extends NodeField

  override val key = AssignmentKey

  override def getType(assignment: NodePath, compilation: Compilation): Node = {
    val target = getAssignmentTarget(assignment)
    ExpressionSkeleton.getType(compilation)(target)
  }

  def createRegistry = new Registry()
  class Registry {
    val assignFromStackByteCodeRegistry = new ShapeRegistry[(Compilation, NodePath) => Seq[Node]]
  }

  override def toByteCode(assignment: NodePath, compilation: Compilation): Seq[Node] = {
    val value = getAssignmentValue(assignment)
    val valueInstructions = ExpressionSkeleton.getToInstructions(compilation)(value)
    val target = getAssignmentTarget(assignment)
    val assignInstructions = getRegistry(compilation).assignFromStackByteCodeRegistry(target.shape)(compilation, target)
    val valueType = ExpressionSkeleton.getType(compilation)(value)
    val duplicateInstruction = TypeSkeleton.getTypeSize(valueType, compilation) match
    {
      case 1 => DuplicateInstructionDelta.duplicate
      case 2 =>  Duplicate2InstructionDelta.duplicate
    }
    valueInstructions ++ Seq(duplicateInstruction) ++ assignInstructions
  }

  override def description: String = "Enables assignment to an abstract target using the = operator."

  override def constraints(compilation: Compilation, builder: ConstraintBuilder, assignment: NodePath, _type: Type, parentScope: Scope): Unit = {
    val value = getAssignmentValue(assignment)
    val valueType = ExpressionSkeleton.getType(compilation, builder, value, parentScope)
    val target = getAssignmentTarget(assignment)
    val targetType = ExpressionSkeleton.getType(compilation, builder, target, parentScope)
    builder.typesAreEqual(targetType, _type)
    builder.checkSubType(targetType, valueType)
  }
}
