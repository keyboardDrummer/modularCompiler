package miksilo.modularLanguages.deltas.bytecode.types

import miksilo.modularLanguages.core.bigrammar.TestLanguageGrammarUtils
import miksilo.modularLanguages.core.deltas.Delta
import miksilo.modularLanguages.core.node.Node
import miksilo.modularLanguages.deltas.bytecode.ByteCodeLanguage
import miksilo.modularLanguages.deltas.bytecode.attributes.CodeAttributeDelta.CodeKey
import miksilo.modularLanguages.deltas.bytecode.attributes.{CodeAttributeDelta, StackMapTableAttributeDelta}
import miksilo.modularLanguages.deltas.bytecode.simpleBytecode.{LabelDelta, LabelledLocations}
import miksilo.modularLanguages.deltas.javac.classes.skeleton.QualifiedClassName
import org.scalatest.funsuite.AnyFunSuite

class TestParseTypes extends AnyFunSuite {

  test("ArrayArrayType") {
    val input = "int[][]"
    val result = TestLanguageGrammarUtils.parse(input, TypeSkeleton.JavaTypeGrammar)
    assertResult(ArrayTypeDelta.neww(ArrayTypeDelta.neww(IntTypeDelta.intType)))(result)
  }

  test("VoidType") {
    val input = "void"
    val result = TestLanguageGrammarUtils.parse(input, TypeSkeleton.JavaTypeGrammar)
    assertResult(VoidTypeDelta.voidType)(result)
  }

  test("appendFrame") {
    val input = "appendFrame int int int"
    val result = TestLanguageGrammarUtils.parse(input, StackMapTableAttributeDelta.StackMapFrameGrammar)
    assertResult(StackMapTableAttributeDelta.AppendFrame)(result.asInstanceOf[Node].shape)
  }

  test("labelWithAppendFrame") {
    val input = "label start-4962768465676381896\n        appendFrame int int"
    val result = new TestLanguageGrammarUtils(Seq[Delta](LabelledLocations) ++ ByteCodeLanguage.byteCodeDeltas).
      parse(input, CodeAttributeDelta.InstructionGrammar)
    assertResult(LabelDelta.Shape)(result.asInstanceOf[Node].shape)
  }

  test("labelWithAppendFrameInInstructions1") {
    val input = "Code: name:9, stack:2, locals:3\n    \n " +
      "label start-4962768465676381896\n        sameFrame\n iload 2 \n    Exceptions:"
    val result = new TestLanguageGrammarUtils(Seq[Delta](LabelledLocations) ++ ByteCodeLanguage.byteCodeDeltas).
      parse(input, CodeAttributeDelta.CodeKey)
    assertResult(CodeKey)(result.asInstanceOf[Node].shape)
  }

  ignore("labelWithAppendFrameInInstructions2") {
    val input = "code: name:9, stack:2, locals:3\n    \n " +
      "label \"start-4962768465676381896\"\n        appendFrame int int\n iload 2 \n    Exceptions:"
    val result = new TestLanguageGrammarUtils(Seq[Delta](LabelledLocations) ++ ByteCodeLanguage.byteCodeDeltas).
      parse(input, CodeAttributeDelta.CodeKey)
    assertResult(CodeKey)(result.asInstanceOf[Node].shape)
  }

  test("intType") {
    val input = "int"
    val result = TestLanguageGrammarUtils.parse(input, TypeSkeleton.JavaTypeGrammar)
    assertResult(IntTypeDelta.intType)(result)
  }

  test("ArrayType") {
    val input = "int[]"
    val result = TestLanguageGrammarUtils.parse(input, TypeSkeleton.JavaTypeGrammar)
    assertResult(ArrayTypeDelta.neww(IntTypeDelta.intType))(result)
  }

  test("ObjectType") {
    val input = "java.lang.String"
    val result = TestLanguageGrammarUtils.parse(input, TypeSkeleton.JavaTypeGrammar)
    val objectType = QualifiedObjectTypeDelta.neww(new QualifiedClassName(Seq("java", "lang", "String")))
    assertResult(objectType)(result)
  }

  test("ArrayType2") {
    val input = "java.lang.String[]"
    val result = TestLanguageGrammarUtils.parse(input, TypeSkeleton.JavaTypeGrammar)
    val objectType = QualifiedObjectTypeDelta.neww(new QualifiedClassName(Seq("java", "lang", "String")))
    assertResult(ArrayTypeDelta.neww(objectType))(result)
  }
}
