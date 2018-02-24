package deltas.bytecode.coreInstructions

import core.language.Compilation
import core.language.node.{Node, NodeShape}
import deltas.bytecode.PrintByteCode._

object InvokeStaticDelta extends InvokeDelta {

  def invokeStatic(constantIndex: Any): Node = shape.create(MethodRef -> constantIndex)

  override def getInstructionSize(compilation: Compilation): Int = 3
  override def getBytes(compilation: Compilation, instruction: Node): Seq[Byte] = {
    hexToBytes("b8") ++ shortToBytes(instruction(MethodRef).asInstanceOf[Int])
  }

  override def description: String = "Defines the invoke static instruction, which can be used to call static methods."

  override def grammarName = "invokestatic"
}
