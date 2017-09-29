package transformations.bytecode.coreInstructions.objects

import core.particles.node.{Key, Node}
import core.particles.{Compilation, Contract, Language}
import transformations.bytecode.PrintByteCode
import transformations.bytecode.attributes.CodeAttribute
import transformations.bytecode.attributes.CodeAttribute.JumpBehavior
import transformations.bytecode.coreInstructions.{InstructionDelta, InstructionSignature}
import transformations.bytecode.simpleBytecode.ProgramTypeState

object AddressReturnInstructionDelta extends InstructionDelta {

  override val key: Key = AddressReturn

  def create: Node = CodeAttribute.instruction(AddressReturn)

  override def jumpBehavior: JumpBehavior = new JumpBehavior(false, false)

  override def getInstructionSize: Int = 1

  override def getSignature(instruction: Node, typeState: ProgramTypeState, state: Compilation): InstructionSignature =
    InstructionSignature(Seq(typeState.stackTypes.head), Seq())

  override def getInstructionByteCode(instruction: Node): Seq[Byte] = PrintByteCode.hexToBytes("b0")

  object AddressReturn extends Key

  override def dependencies: Set[Contract] = super.dependencies ++ Set()

  override def description: String = "Defines the address return instruction, which returns a reference from the current method."

  override def grammarName = "areturn"
}
