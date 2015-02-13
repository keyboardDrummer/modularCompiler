package application.compilerCockpit

import core.transformation.{CompilerFromParticles, TransformationState, MetaObject}
import core.transformation.sillyCodePieces.Particle
import transformations.bytecode.ByteCodeSkeleton
import transformations.javac.classes.QualifiedClassName
import util.TestUtils

object RunWithJVM extends Particle
{
  override def inject(state: TransformationState): Unit = {
    state.compilerPhases ::= (() => {
      val clazz: MetaObject = state.program
      val classRefIndex = ByteCodeSkeleton.getClassNameIndex(clazz)
      val constantPool = ByteCodeSkeleton.getConstantPool(clazz)
      val classNameIndex = ByteCodeSkeleton.getClassRefName(constantPool.getValue(classRefIndex).asInstanceOf[MetaObject])
      val className = constantPool.getValue(classNameIndex).asInstanceOf[QualifiedClassName].toString
      state.output = TestUtils.runByteCode(className, clazz)
    })
  }
}
object CompileAndRunOption extends CompileOption {

  override def perform(cockpit: CompilerCockpit, input: String): String = {
    val compiler = new CompilerFromParticles(cockpit.particles ++ Seq(RunWithJVM))
    val state = compiler.parseAndTransform(input)

    state.output
  }

  override def toString = "Compile and run"
}