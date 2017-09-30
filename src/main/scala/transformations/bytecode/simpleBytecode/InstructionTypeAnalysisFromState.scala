package transformations.bytecode.simpleBytecode

import core.particles.Compilation
import core.particles.node.Node
import transformations.bytecode.ByteCodeMethodInfo.ByteCodeMethodInfoWrapper
import transformations.bytecode.attributes.CodeAttribute
import transformations.bytecode.attributes.CodeAttribute.JumpBehavior
import transformations.bytecode.constants.ClassInfoConstant
import transformations.bytecode.coreInstructions.InstructionSignature
import transformations.bytecode.extraConstants.QualifiedClassNameConstant
import transformations.bytecode.simpleBytecode.InstructionTypeAnalysis.InstructionSideEffects
import transformations.bytecode.types.ObjectTypeDelta
import transformations.bytecode.{ByteCodeMethodInfo, ByteCodeSkeleton}
import transformations.javac.classes.skeleton.QualifiedClassName

class InstructionTypeAnalysisFromState(state: Compilation, method: ByteCodeMethodInfoWrapper[Node]) {
  val typeAnalysis = getTypeAnalysis
  val parameters = getMethodParameters
  val initialVariables = parameters.zipWithIndex.map(p => p._2 -> p._1).toMap
  val initialStack = Seq[Node]()
  val initialProgramTypeState: ProgramTypeState = ProgramTypeState(initialStack, initialVariables)
  val typeStatePerInstruction = typeAnalysis.run(0, initialProgramTypeState)

  private def getTypeAnalysis = {
    val codeAnnotation = method.attributes.find(a => a.clazz == CodeAttribute.CodeKey).get
    val instructions = CodeAttribute.getCodeInstructions(codeAnnotation)

    new InstructionTypeAnalysis(instructions) {
      val instructionVariableUpdateRegistry = CodeAttribute.getState(state).localUpdates
      override def getSideEffects(typeState: ProgramTypeState, instruction: Node): InstructionSideEffects =
        instructionVariableUpdateRegistry(instruction.clazz).getVariableUpdates(instruction, typeState)

      val instructionSignatureRegistry = CodeAttribute.getInstructionSignatureRegistry(state)
      override def getSignature(typeState: ProgramTypeState, instruction: Node): InstructionSignature =
        instructionSignatureRegistry(instruction.clazz).getSignature(instruction, typeState, state)

      val jumpBehaviorRegistry = CodeAttribute.getState(state).jumpBehaviorRegistry
      override def getJumpBehavior(instructionClazz: Any): JumpBehavior = jumpBehaviorRegistry(instructionClazz)
    }
  }
  
  private def getMethodParameters = {
    val methodIsStatic: Boolean = method.accessFlags.contains(ByteCodeMethodInfo.StaticAccess)
    val methodParameters = method._type.parameterTypes
    if (methodIsStatic) {
      methodParameters
    }
    else {
      val clazz = state.program
      val clazzRef = clazz(ByteCodeSkeleton.ClassNameIndexKey).asInstanceOf[Node]
      val className = clazzRef(ClassInfoConstant.Name).asInstanceOf[Node]
      Seq(ObjectTypeDelta.objectType(className(QualifiedClassNameConstant.Value).asInstanceOf[QualifiedClassName])) ++ methodParameters
    }
  }
}
