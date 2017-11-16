package deltas.javac

import core.deltas.node.Node
import deltas.bytecode.types.{ArrayTypeC, ObjectTypeDelta, VoidTypeC}
import deltas.javac.classes.skeleton.JavaClassSkeleton._
import deltas.javac.classes.skeleton.QualifiedClassName
import deltas.javac.methods.AccessibilityFieldsDelta
import deltas.javac.methods.MethodDelta._
import org.scalatest.FunSuite
import util.{CompilerBuilder, SourceUtils}

class EmptyMain extends FunSuite {
  val className = "EmptyMain"
  val defaultPackage = Seq()
  val other = new FibonacciWithoutMain()

  test("runCompiledCode") {
    val byteCode: Node = getByteCode
    SourceUtils.runByteCode(className, byteCode)
  }

  def getByteCode: Node = {
    val java = getJava
    val byteCode = CompilerBuilder.build(JavaCompilerDeltas.javaCompilerDeltas).transform(java).program
    byteCode
  }

  def getJava: Node = {
    clazz(defaultPackage, className, Seq(getMainMethodJava))
  }

  def getMainMethodJava: Node = {
    val parameters = Seq(parameter("args", ArrayTypeC.arrayType(ObjectTypeDelta.objectType(QualifiedClassName(Seq("java", "lang", "String"))))))
    val body = Seq()
    method("main", VoidTypeC.voidType, parameters, body, static = true, AccessibilityFieldsDelta.PublicVisibility)
  }
}
