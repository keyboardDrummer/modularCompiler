package transformations.javac

import transformations.bytecode.constants.MethodDescriptorConstant
import transformations.javac.classes.{MethodInfo, PackageInfo, QualifiedClassName}
import transformations.javac.constructor.SuperCallExpression
import transformations.types.{IntTypeC, ObjectTypeC, VoidTypeC}

object JavaLang {
  val javaPackageName: String = "java"
  val langPackageName: String = "lang"
  val ioPackageName = "io"
  val standardLib = new PackageInfo(None, "")
  val javaPackage = standardLib.newPackageInfo(javaPackageName)
  val langPackage = javaPackage.newPackageInfo(langPackageName)
  val objectClass = langPackage.newClassInfo(ImplicitObjectSuperClass.objectName)
  objectClass.content(SuperCallExpression.constructorName) =
    new MethodInfo(MethodDescriptorConstant.methodDescriptor(VoidTypeC.voidType, Seq()), false)

  val systemClass = langPackage.newClassInfo("System")
  systemClass.newFieldInfo("out", ObjectTypeC.objectType(new QualifiedClassName(Seq(javaPackageName, ioPackageName, "PrintStream"))))

  val javaIO = javaPackage.newPackageInfo(ioPackageName)
  val printStreamClass = javaIO.newClassInfo("PrintStream")
  printStreamClass.newMethodInfo("print", MethodDescriptorConstant.methodDescriptor(VoidTypeC.voidType, Seq(IntTypeC.intType)), _static = false)
  printStreamClass.newMethodInfo("println", MethodDescriptorConstant.methodDescriptor(VoidTypeC.voidType, Seq(IntTypeC.intType)), _static = false)

  val stringClass = langPackage.newClassInfo("String")
}
