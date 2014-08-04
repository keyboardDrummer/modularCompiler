package transformations.bytecode.instructions

import core.transformation.MetaObject
import transformations.bytecode.ByteCodeSkeleton
import transformations.bytecode.ByteCodeSkeleton._
import transformations.bytecode.PrintByteCode._
import transformations.javac.base.ConstantPool
import transformations.javac.base.model.JavaTypes

object IfIntegerCompareGreaterC extends InstructionC {

  override val key: Any = IfIntegerCompareGreaterKey

  def ifIntegerCompareGreater(target: Int): MetaObject = instruction(IfIntegerCompareGreaterKey, Seq(target))

  override def getInstructionStackSizeModification(constantPool: ConstantPool, instruction: MetaObject): Int = -2

  override def getInstructionByteCode(instruction: MetaObject): Seq[Byte] = {
    val arguments = ByteCodeSkeleton.getInstructionArguments(instruction)
    hexToBytes("a2") ++ shortToBytes(arguments(0))
  }

  override def getInstructionInAndOutputs(constantPool: ConstantPool, instruction: MetaObject): (Seq[MetaObject], Seq[MetaObject]) =
    (Seq(JavaTypes.intType, JavaTypes.intType), Seq())

  override def getInstructionSize: Int = 3

  object IfIntegerCompareGreaterKey

}
