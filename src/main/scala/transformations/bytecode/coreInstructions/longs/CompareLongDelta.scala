package transformations.bytecode.coreInstructions.longs

import core.particles.Compilation
import core.particles.node.{Node, NodeClass}
import transformations.bytecode.PrintByteCode
import transformations.bytecode.coreInstructions.{InstructionDelta, InstructionSignature}
import transformations.bytecode.simpleBytecode.ProgramTypeState
import transformations.bytecode.types.{IntTypeC, LongTypeC}

object CompareLongDelta extends InstructionDelta {

  val compareLong = new Node(CompareLongKey)

  object CompareLongKey extends NodeClass

  override val key = CompareLongKey

  override def getInstructionByteCode(instruction: Node): Seq[Byte] = {
    PrintByteCode.hexToBytes("94")
  }

  override def getSignature(instruction: Node, typeState: ProgramTypeState, state: Compilation): InstructionSignature =
    InstructionSignature(Seq(LongTypeC.longType, LongTypeC.longType), Seq(IntTypeC.intType))

  override def grammarName = "lcmp"
}
