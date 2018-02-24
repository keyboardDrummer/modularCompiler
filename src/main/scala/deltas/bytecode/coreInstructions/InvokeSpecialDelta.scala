package deltas.bytecode.coreInstructions

import core.language.node.Node
import core.language.{Compilation, Language}
import deltas.bytecode.PrintByteCode._
import deltas.bytecode.simpleBytecode.ProgramTypeState

/**
 * Invokes an instance method using static binding, so no dynamic dispatch is applied.
 */
object InvokeSpecialDelta extends InvokeDelta {

  def invokeSpecial(location: Any): Node = shape.create(MethodRef -> location)

  override def getBytes(compilation: Compilation, instruction: Node): Seq[Byte] = {
    hexToBytes("b7") ++ shortToBytes(instruction(MethodRef).asInstanceOf[Int])
  }

  override def getInstructionSize(compilation: Compilation): Int = 3
  override def getSignature(instruction: Node, typeState: ProgramTypeState, language: Language): InstructionSignature = {
    getInstanceInstructionSignature(instruction, typeState, language)
  }

  override def description: String = "Defines the invoke special method, which can be used to call constructors."

  override def grammarName = "invokespecial"
}