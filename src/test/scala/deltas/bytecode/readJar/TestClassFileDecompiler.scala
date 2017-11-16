package deltas.bytecode.readJar

import java.io

import application.compilerCockpit.PrettyPrint
import core.bigrammar.TestCompilerGrammarUtils
import core.deltas.DeltasToParserConverter
import core.deltas.node.Node
import org.scalatest.FunSuite
import deltas.bytecode.types.TypeSkeleton
import deltas.bytecode.types.TypeSkeleton.ByteCodeTypeGrammar
import deltas.javac.JavaCompilerDeltas
import deltas.javac.types.TypeAbstraction
import util.{CompilerBuilder, SourceUtils}

import scala.reflect.io.{File, Path}

class TestClassFileDecompiler extends FunSuite {

  test("TypeVariableSimilarToBooleanSignature") {
    val signature = "<B:Ljava/lang/Object;V:Ljava/lang/Object;>(Ljava/lang/Class<TB;>;Ljava/lang/String;Ljava/lang/String;)Lcom/sun/xml/internal/bind/api/RawAccessor<TB;TV;>;"
    val compiler = CompilerBuilder.build(ClassFileSignatureDecompiler.getDecompiler)
    val state = compiler.buildLanguage

    val manager = new DeltasToParserConverter()
    val result = manager.parse(state.grammars.find(TypeAbstraction.AbstractMethodTypeGrammar), signature).asInstanceOf[Node]
  }

  test("TypeVariable") {
    val signature = "<NoSuchMemberException:Ljava/lang/ReflectiveOperationException;>(BLjava/lang/invoke/MemberName;Ljava/lang/Class<*>;Ljava/lang/Class<TNoSuchMemberException;>;)Ljava/lang/invoke/MemberName;^Ljava/lang/IllegalAccessException;"
    val compiler = CompilerBuilder.build(ClassFileSignatureDecompiler.getDecompiler)
    val state = compiler.buildLanguage

    val manager = new DeltasToParserConverter()
    val result = manager.parse(state.grammars.find(TypeSkeleton.ByteCodeTypeGrammar), signature).asInstanceOf[Node]
  }

  test("TypeVariable2") {
    val signature = "<NoSuchMemberException:Ljava/lang/ReflectiveOperationException;>(BLjava/lang/invoke/MemberName;Ljava/lang/Class<*>;Ljava/lang/Class<TNoSuchMemberException;>;)Ljava/lang/invoke/MemberName;^Ljava/lang/IllegalAccessException;^TNoSuchMemberException;"
    val compiler = CompilerBuilder.build(ClassFileSignatureDecompiler.getDecompiler)
    val state = compiler.buildLanguage

    val manager = new DeltasToParserConverter()
    val result = manager.parse(state.grammars.find(TypeSkeleton.ByteCodeTypeGrammar), signature).asInstanceOf[Node]
  }

  test("TypeVariable3") {
    val signature = "(BLjava/lang/invoke/MemberName;Ljava/lang/Class<*>;Ljava/lang/Class<TNoSuchMemberException;>;)Ljava/lang/invoke/MemberName;^Ljava/lang/IllegalAccessException;^TNoSuchMemberException;"
    val compiler = CompilerBuilder.build(ClassFileSignatureDecompiler.getDecompiler)
    val state = compiler.buildLanguage

    val manager = new DeltasToParserConverter()
    val result = manager.parse(state.grammars.find(TypeSkeleton.ByteCodeTypeGrammar), signature).asInstanceOf[Node]
  }

  ignore("decompileRuntimeJar") {
    val currentDir = new File(new io.File("."))
    val testResources = currentDir / Path("resources") / "rtUnzipped"
    val allCassFiles = testResources.toDirectory.deepFiles
    val compiler = CompilerBuilder.build(/*Seq(new PrettyPrint()) ++*/ ClassFileSignatureDecompiler.getDecompiler)
    var counter = 0
    val start = 17453
    for(file <- allCassFiles) {
      if (counter >= start && file.extension.contains("class")) {
        val inputStream = file.inputStream()
        Console.println(s"starting: ${file.name}")
        compiler.parseAndTransform(inputStream)
        Console.println(s"progress: $counter / 19.626")
      }
      counter += 1
    }
  }

  test("ObjectClassUnParsedAttributes") {
    val inputStream = SourceUtils.getTestFile("Object.class")
    val compiler = CompilerBuilder.build(Seq(new PrettyPrint()) ++ ClassFileSignatureDecompiler.byteCodeParticles)
    val state = compiler.parseAndTransform(inputStream)

    val expected = SourceUtils.getTestFileContents("DecodedObjectClassPrettyPrint.txt")
    assertResult(expected)(state.output)
  }

  test("ObjectClassParsedAttributes") {
    val compiler = CompilerBuilder.build(Seq(ParseKnownAttributes) ++ Seq(new PrettyPrint()) ++
      ClassFileSignatureDecompiler.onlySignatureAttribute)
    val state = compiler.parseAndTransform(SourceUtils.getTestFile("Object.class"))

    val expected = SourceUtils.getTestFileContents("DecodedWithAttributesObjectClassPrettyPrint.txt")
    assertResult(expected)(state.output)
  }

  test("ObjectClassSignatureDeCompilation") {
    val compiler = CompilerBuilder.build(ClassFileSignatureDecompiler.getDecompiler)
    val state = compiler.parseAndTransform(SourceUtils.getTestFile("Object.class"))
    val output = CompilerBuilder.build(Seq(new PrettyPrint()) ++ JavaCompilerDeltas.javaCompilerDeltas).transform(state.program).output

    val expected = SourceUtils.getTestFileContents("DecompiledObjectClassFileSignature.txt")
    assertResult(expected)(output)
  }

  test("StringClassSignatureDeCompilation") {
    val compiler = CompilerBuilder.build(ClassFileSignatureDecompiler.getDecompiler)
    val state = compiler.parseAndTransform(SourceUtils.getTestFile("String2.class"))
    val output = CompilerBuilder.build(Seq(new PrettyPrint()) ++ JavaCompilerDeltas.javaCompilerDeltas).transform(state.program).output

    val expected = SourceUtils.getTestFileContents("DecompiledStringClassFileSignature.txt")
    assertResult(expected)(output)
  }

  test("SystemClassSignatureDeCompilation") {
    val compiler = CompilerBuilder.build(ClassFileSignatureDecompiler.getDecompiler)
    val state = compiler.parseAndTransform(SourceUtils.getTestFile("System.class"))
    val output = CompilerBuilder.build(Seq(new PrettyPrint()) ++ JavaCompilerDeltas.javaCompilerDeltas).transform(state.program).output

    val expected = SourceUtils.getTestFileContents("DecompiledSystemClassFileSignature.txt")
    assertResult(expected)(output)
  }

  test("PrintStreamClassSignatureDeCompilation") {
    val compiler = CompilerBuilder.build(ClassFileSignatureDecompiler.getDecompiler)
    val state = compiler.parseAndTransform(SourceUtils.getTestFile("PrintStream.class"))
    val output = CompilerBuilder.build(Seq(new PrettyPrint()) ++ JavaCompilerDeltas.javaCompilerDeltas).transform(state.program).output

    val expected = SourceUtils.getTestFileContents("DecompiledPrintStreamClassFileSignature.txt")
    assertResult(expected)(output)
  }

  test("ParseByteCodeType3") {
    val grammarUtils = TestCompilerGrammarUtils(ClassFileSignatureDecompiler.getDecompiler)
    grammarUtils.compareInputWithPrint("([BII)V", None, ByteCodeTypeGrammar)
  }


  test("ParseByteCodeType2") {
    val grammarUtils = TestCompilerGrammarUtils(ClassFileSignatureDecompiler.getDecompiler)
    grammarUtils.compareInputWithPrint("[B", None, ByteCodeTypeGrammar)
  }

  test("ParseByteCodeType1") {
    val grammarUtils = TestCompilerGrammarUtils(ClassFileSignatureDecompiler.getDecompiler)
    grammarUtils.compareInputWithPrint("B", None, ByteCodeTypeGrammar)
  }

  test("ParseByteCodeType0") {
    val grammarUtils = TestCompilerGrammarUtils(ClassFileSignatureDecompiler.getDecompiler)
    grammarUtils.compareInputWithPrint("I", None, ByteCodeTypeGrammar)
  }
}