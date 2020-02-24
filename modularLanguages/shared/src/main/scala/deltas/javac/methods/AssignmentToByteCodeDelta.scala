package deltas.javac.methods

import core.deltas.ShapeProperty
import core.deltas.path.NodePath
import core.language.Compilation
import core.language.node.Node
import deltas.bytecode.coreInstructions.integers.StoreIntegerDelta
import deltas.bytecode.coreInstructions.objects.StoreAddressDelta
import deltas.bytecode.coreInstructions.{Duplicate2InstructionDelta, DuplicateInstructionDelta}
import deltas.bytecode.types.TypeSkeleton
import deltas.expression.ExpressionDelta
import deltas.javac.expressions.{ConvertsToByteCodeDelta, ToByteCodeSkeleton}
import deltas.statement.assignment.SimpleAssignmentDelta
import deltas.statement.assignment.SimpleAssignmentDelta.{getTarget, getValue}

trait HasAssignFromStackByteCode {
  def getAssignFromStackByteCode(compilation: Compilation, path: NodePath): Seq[Node]
}

object AssignmentToByteCodeDelta extends ConvertsToByteCodeDelta {
  val hasAssignFromStackByteCode = new ShapeProperty[HasAssignFromStackByteCode]

  override def toByteCode(assignment: NodePath, compilation: Compilation): Seq[Node] = {
    val value = getValue(assignment)
    val valueInstructions = ToByteCodeSkeleton.getToInstructions(compilation)(value)
    val target = getTarget(assignment)
    val assignInstructions = hasAssignFromStackByteCode(compilation, target.shape).getAssignFromStackByteCode(compilation, target)
    val valueType = ExpressionDelta.cachedNodeType(compilation, value)
    val duplicateInstruction = TypeSkeleton.getTypeSize(valueType, compilation) match
    {
      case 1 => DuplicateInstructionDelta.duplicate
      case 2 =>  Duplicate2InstructionDelta.duplicate
    }
    valueInstructions ++ Seq(duplicateInstruction) ++ assignInstructions
  }

  override def description = "Allows converting assignments to bytecode"

  override def dependencies = Set(StoreAddressDelta, StoreIntegerDelta, SimpleAssignmentDelta)

  override def shape = SimpleAssignmentDelta.Shape
}