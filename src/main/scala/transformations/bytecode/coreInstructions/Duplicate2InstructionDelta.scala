package transformations.bytecode.coreInstructions

import core.particles.Compilation
import core.particles.node.{Node, NodeClass}
import transformations.bytecode.PrintByteCode
import transformations.bytecode.attributes.CodeAttribute
import transformations.bytecode.simpleBytecode.ProgramTypeState

object Duplicate2InstructionDelta extends InstructionDelta {

  object Duplicate2Key extends NodeClass
  def duplicate = CodeAttribute.instruction(Duplicate2Key, Seq.empty)

  override val key = Duplicate2Key

  override def getInstructionByteCode(instruction: Node): Seq[Byte] = {
    PrintByteCode.hexToBytes("5c")
  }

  override def getSignature(instruction: Node, typeState: ProgramTypeState, state: Compilation): InstructionSignature = {
    val input: Node = typeState.stackTypes.last
    assertDoubleWord(state, input)
    new InstructionSignature(Seq(input),Seq(input, input))
  }

  override def description: String = "Defines the duplicate2 instruction, which duplicates the top two stack values."

  override def grammarName = "dup2"
}
