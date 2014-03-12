package languages.javac

import transformation.{TransformationState, MetaObject, ProgramTransformation, TransformationManager}
import languages.javac.base.JavaBase
import languages.javac.TernaryC
import languages.bytecode.{NoStackFrame, NoMaxStack, ByteCodeGoTo}

object JavaCompiler {
  def getCompiler = TransformationManager.buildCompiler(javaCompilerTransformations)

  def javaCompilerTransformations: Seq[ProgramTransformation] = {
    Seq(ImplicitThisInPrivateCalls, ImplicitJavaLangImport, DefaultConstructor, ImplicitSuperConstructorCall, ImplicitObjectSuperClass, ImplicitReturnAtEndOfMethod, ConstructorC, LessThanC,
      AdditionC, LiteralC, SubtractionC, TernaryC, JavaBase, NoStackFrame, NoMaxStack, ByteCodeGoTo)
  }
}

object JavaMinus extends ProgramTransformation {
  override def dependencies: Set[ProgramTransformation] =
    Set(ImplicitThisInPrivateCalls, ImplicitJavaLangImport, ImplicitSuperConstructorCall, ImplicitObjectSuperClass, ImplicitReturnAtEndOfMethod, DefaultConstructor, LessThanC,
      AdditionC, LiteralC, SubtractionC, TernaryC)

  override def transform(program: MetaObject, state: TransformationState): Unit = {

  }
}
