package transformations.bytecode.coreInstructions

import core.transformation._
import core.transformation.sillyCodePieces.Injector
import transformations.bytecode.ByteCodeSkeleton.JumpBehavior
import transformations.bytecode._
import transformations.javac.classes.ConstantPool
import transformations.types.TypeC

trait InstructionC extends Injector {

  override def inject(state: TransformationState): Unit = {
    ByteCodeSkeleton.getInstructionSignatureRegistry(state).put(key, (c,i) => getInstructionInAndOutputs(c,i,state))
    ByteCodeSkeleton.getInstructionStackSizeModificationRegistry(state).put(key, (c, i) => getInstructionStackSizeModification(c, i, state))
    PrintByteCode.getBytesRegistry(state).put(key, getInstructionByteCode)
    ByteCodeSkeleton.getInstructionSizeRegistry(state).put(key, getInstructionSize)
    ByteCodeSkeleton.getState(state).jumpBehaviorRegistry.put(key, getJumpBehavior)
    ByteCodeSkeleton.getState(state).localUpdates.put(key, getVariableUpdates)
  }

  override def dependencies: Set[Contract] = Set(ByteCodeSkeleton)

  val key: AnyRef

  def getVariableUpdates(instruction: MetaObject): Map[Int, MetaObject] = Map.empty

  def getInstructionInAndOutputs(constantPool: ConstantPool, instruction: MetaObject, state: TransformationState): (Seq[MetaObject], Seq[MetaObject])

  def getInstructionSize(instruction: MetaObject): Int = getInstructionByteCode(instruction).size

  def getJumpBehavior: JumpBehavior = new JumpBehavior(true, false)

  def getInstructionByteCode(instruction: MetaObject): Seq[Byte]

  def getInstructionStackSizeModification(constantPool: ConstantPool, instruction: MetaObject, state: TransformationState): Int = {
    val inAndOutputs = getInstructionInAndOutputs(constantPool, instruction, state)
    inAndOutputs._2.map(t => TypeC.getTypeSize(t,state)).sum - inAndOutputs._1.map(t => TypeC.getTypeSize(t,state)).sum
  }


  protected def binary(_type: MetaObject) = (Seq(_type, _type), Seq(_type))
}
