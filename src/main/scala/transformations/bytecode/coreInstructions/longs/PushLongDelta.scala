package transformations.bytecode.coreInstructions.longs

import core.particles.Compilation
import core.particles.node.{Node, NodeClass}
import transformations.bytecode.PrintByteCode._
import transformations.bytecode.attributes.CodeAttribute
import transformations.bytecode.coreInstructions.{InstructionDelta, InstructionSignature}
import transformations.bytecode.simpleBytecode.ProgramTypeState
import transformations.bytecode.types.LongTypeC

object PushLongDelta extends InstructionDelta {

  override val key = LongConstantKey

  def constant(value: Int) = {
    require (0 <= value && value <= 1)
    CodeAttribute.instruction(LongConstantKey, Seq(value))
  }

  override def getInstructionByteCode(instruction: Node): Seq[Byte] = {
    byteToBytes(9 + CodeAttribute.getInstructionArguments(instruction).head)
  }

  override def getSignature(instruction: Node, typeState: ProgramTypeState, state: Compilation): InstructionSignature =
    InstructionSignature(Seq(), Seq(LongTypeC.longType))

  override def getInstructionSize: Int = 1

  object LongConstantKey extends NodeClass

  override def grammarName = "lconst" //TODO lconst_0 & lconst_1
}

