package transformations.bytecode.coreInstructions

import core.transformation.MetaObject
import transformations.bytecode.{PrintByteCode, ByteCodeSkeleton}
import transformations.bytecode.ByteCodeSkeleton._
import PrintByteCode._

object InvokeStaticC extends InvokeC {

  override val key: AnyRef = InvokeStaticKey

  def invokeStatic(constantIndex: Int): MetaObject = instruction(InvokeStaticKey, Seq(constantIndex))

  override def getInstructionByteCode(instruction: MetaObject): Seq[Byte] = {
    val arguments = ByteCodeSkeleton.getInstructionArguments(instruction)
    hexToBytes("b8") ++ shortToBytes(arguments(0))
  }

  object InvokeStaticKey

  override def description: String = "Defines the invoke static instruction, which can be used to call static methods."
}
