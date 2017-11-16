package deltas.bytecode.additions

import core.deltas.node.Node
import core.deltas.{Compilation, Contract, DeltaWithPhase, Language}
import deltas.bytecode.ByteCodeSkeleton.ByteCodeWrapper
import deltas.bytecode.attributes.CodeAttribute
import deltas.bytecode.coreInstructions._
import deltas.bytecode.simpleBytecode.InstructionTypeAnalysisFromState
import deltas.bytecode.types.TypeSkeleton

object PoptimizeC extends DeltaWithPhase {

  override def dependencies: Set[Contract] = Set(PopDelta)

  private def getSignatureInOutLengths(state: Language, signature: InstructionSignature): (Int, Int) = {
    val inputLength = signature.inputs.map(_type => TypeSkeleton.getTypeSize(_type, state)).sum
    val outputLength = signature.outputs.map(_type => TypeSkeleton.getTypeSize(_type, state)).sum
    (inputLength, outputLength)
  }

  override def transform(_clazz: Node, state: Compilation): Unit = {
    val clazz = new ByteCodeWrapper(_clazz)
    for (method <- clazz.methods) {
      val typeAnalysis = new InstructionTypeAnalysisFromState(state, method)
      val codeAnnotation = method.codeAttribute
      val instructions = codeAnnotation.instructions

      def getInOutSizes(instructionIndex: Int) = {
        val instruction = instructions(instructionIndex)
        val signatureProvider = CodeAttribute.getInstructionSignatureRegistry(state.language)(instruction.clazz)
        val signature = signatureProvider.getSignature(instruction, typeAnalysis.typeStatePerInstruction(instructionIndex), state)
        getSignatureInOutLengths(state.language, signature)
      }

      var newInstructions = List.empty[Node]
      var consumptions = List.empty[Boolean]

      def processInstruction(instructionIndex: Int) {
        val instruction = instructions(instructionIndex)
        if (instruction.clazz == PopDelta.PopKey) {
          consumptions ::= true
          return
        }

        if (instruction.clazz == Pop2Delta.Pop2Key) {
          consumptions = List(true,true) ++ consumptions
          return
        }

        val (in, out) = getInOutSizes(instructionIndex)
        var outLeft = out
        var outPop = 0
        while (outLeft > 0 && consumptions.nonEmpty) {
          val pop = consumptions.head
          outPop += (if (pop) 1 else 0)
          consumptions = consumptions.tail
          outLeft -= 1
        }
        val outConsumption = out - outPop
        val hasSideEffect = guessIfInstructionHasSideEffect(out)
        val keepInstruction = outConsumption != 0 || hasSideEffect
        if (keepInstruction) {
          val pop2Instructions = 0.until(outPop / 2).map(_ => Pop2Delta.pop2).toList
          val pop1Instructions: ((Nothing) => Any) with Iterable[Node] = if (outPop % 2 == 1) Seq(PopDelta.pop) else Set.empty
          newInstructions = pop2Instructions ++ pop1Instructions ++ newInstructions
          consumptions = 0.until(in).map(_ => false).toList ++ consumptions
          newInstructions = instruction :: newInstructions
        }
      }

      for (instructionIndex <- instructions.indices.reverse) {
        processInstruction(instructionIndex)
      }
      codeAnnotation(CodeAttribute.Instructions) = newInstructions.toSeq
    }
  }

  def guessIfInstructionHasSideEffect(out: Int): Boolean = {
    out == 0 //dangerous assumption :D
  }

  override def description: String = "Optimizes a bytecode program by removing instructions in cases where an instructions output will always be consumed by a pop."
}