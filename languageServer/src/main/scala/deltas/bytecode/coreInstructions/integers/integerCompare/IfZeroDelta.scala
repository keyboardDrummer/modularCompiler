package deltas.bytecode.coreInstructions.integers.integerCompare

import core.language.node.Node
import core.deltas.Contract
import core.language.{Compilation, Language}
import deltas.bytecode.PrintByteCode._
import deltas.bytecode.attributes.CodeAttributeDelta
import deltas.bytecode.coreInstructions.InstructionSignature
import deltas.bytecode.simpleBytecode.ProgramTypeState
import deltas.bytecode.types.IntTypeDelta

object IfZeroDelta extends JumpInstruction {

  def ifZero(target: Int) = CodeAttributeDelta.instruction(shape, Seq(target))

  override def getBytes(compilation: Compilation, instruction: Node): Seq[Byte] = {
    val arguments = CodeAttributeDelta.getInstructionArguments(instruction)
    hexToBytes("99") ++ shortToBytes(arguments.head)
  }

  override def getSignature(instruction: Node, typeState: ProgramTypeState, language: Language): InstructionSignature =
    InstructionSignature(Seq(IntTypeDelta.intType), Seq())

  override def dependencies: Set[Contract] = super.dependencies ++ Set(IntTypeDelta)

  override def grammarName = "ifeq"
}