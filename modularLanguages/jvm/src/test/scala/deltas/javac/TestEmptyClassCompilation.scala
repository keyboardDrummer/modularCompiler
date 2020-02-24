package deltas.javac

import core.deltas.path.PathRoot
import core.language.node.Node
import deltas.bytecode.attributes.CodeAttributeDelta
import deltas.bytecode.constants._
import deltas.bytecode.coreInstructions.objects.LoadAddressDelta
import deltas.bytecode.coreInstructions.{InvokeSpecialDelta, VoidReturnInstructionDelta}
import deltas.bytecode.extraConstants.TypeConstant
import deltas.bytecode.types.VoidTypeDelta
import deltas.bytecode.{ByteCodeMethodInfo, ByteCodeSkeleton}
import deltas.javac.classes.ConstantPool
import deltas.javac.classes.skeleton.{JavaClassDelta, QualifiedClassName}
import deltas.javac.constructor.ConstructorDelta
import deltas.javac.types.MethodTypeDelta
import util.{JavaLanguageTest, LanguageTest, TestLanguageBuilder}

class TestEmptyClassCompilation extends JavaLanguageTest {
  val className: String = "EmptyClass"
  val classPackage: Seq[String] = Seq("transformations", "java", "testing")

  test("EquivalentConstantPool") {
    val expectedByteCode = getEmptyClassByteCode
    val javaCode: Node = getEmptyClass
    val java = TestLanguageBuilder.build(JavaToByteCodeLanguage.javaCompilerDeltas)
    val compiledCode = java.compileAst(javaCode).program.asInstanceOf[PathRoot].current
    compareConstantPools(expectedByteCode, compiledCode)
  }

  test("EquivalentMethod") {
    val expectedByteCode = getEmptyClassByteCode
    val javaCode = getEmptyClass
    val java = TestLanguageBuilder.build(JavaToByteCodeLanguage.javaCompilerDeltas)
    val compiledCode = java.compileAst(javaCode).program.asInstanceOf[PathRoot].current

    LanguageTest.testInstructionEquivalence(expectedByteCode, compiledCode)
  }

  def getEmptyClassByteCode: Node = {
    val constantPool = new ConstantPool(Seq(MethodRefConstant.methodRef(3, 10),
      ClassInfoConstant.classRef(11),
      ClassInfoConstant.classRef(12),
      Utf8ConstantDelta.create(ConstructorDelta.constructorName),
      TypeConstant.constructor(MethodTypeDelta.neww(VoidTypeDelta.voidType, Seq())),
      CodeAttributeDelta.constantEntry,
      NameAndTypeConstant.nameAndType(4, 5),
      Utf8ConstantDelta.fromQualifiedClassName(QualifiedClassName(Seq("transformations", "java", "testing", "EmptyClass"))),
      Utf8ConstantDelta.fromQualifiedClassName(QualifiedClassName(Seq("java", "lang", "Object"))))
    )
    val instructions = Seq(LoadAddressDelta.addressLoad(0), InvokeSpecialDelta.invokeSpecial(1), VoidReturnInstructionDelta.voidReturn)
    val codeAttribute = Seq(CodeAttributeDelta.codeAttribute(5, 1, 1, instructions, Seq(), Seq()))
    val defaultConstructor = ByteCodeMethodInfo.methodInfo(3, 4, codeAttribute, Set(ByteCodeMethodInfo.PublicAccess))
    ByteCodeSkeleton.neww(2, 3, constantPool, Seq(defaultConstructor))
  }

  def getEmptyClass: Node = {
    JavaClassDelta.neww(classPackage, className, members = Seq[Node]())
  }
}