package deltas.bytecode.coreInstructions.integers

import core.deltas.{Compilation, Language}
import core.deltas.node.{Node, NodeClass}
import deltas.bytecode.PrintByteCode._
import deltas.bytecode.attributes.CodeAttributeDelta
import deltas.bytecode.coreInstructions.{InstructionDelta, InstructionSignature}
import deltas.bytecode.simpleBytecode.ProgramTypeState

object IncrementIntegerDelta extends InstructionDelta {

  override val key = IntegerIncrementKey

  def integerIncrement(location: Int, amount: Int) = CodeAttributeDelta.instruction(IntegerIncrementKey, Seq(location, amount))

  override def getInstructionByteCode(instruction: Node): Seq[Byte] = {
    val arguments = CodeAttributeDelta.getInstructionArguments(instruction)
    hexToBytes("84") ++
      byteToBytes(arguments(0)) ++
      byteToBytes(arguments(1))
  }

  override def getSignature(instruction: Node, typeState: ProgramTypeState, language: Language): InstructionSignature =
    InstructionSignature(Seq.empty, Seq.empty)

  override def getInstructionSize(): Int = 3

  object IntegerIncrementKey extends NodeClass

  override def description: String = "Defines the increment integer instruction, which increments an integer variable by a specific amount."

  override def grammarName = "iinc"
}
