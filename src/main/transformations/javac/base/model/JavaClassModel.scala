package transformations.javac.base.model

import core.transformation.MetaObject
import transformations.bytecode.ByteCodeSkeleton

import scala.collection.mutable

object JavaClassModel {
  def getPackage(clazz: MetaObject): Seq[String] = clazz(ClassPackage).asInstanceOf[Seq[String]]

  def getImports(clazz: MetaObject) = clazz(ClassImports).asInstanceOf[List[JavaImport]]

  def clazz(_package: Seq[String], name: String, methods: Seq[MetaObject] = Seq(), imports: List[JavaImport] = List(), mbParent: Option[String] = None) = new MetaObject(ByteCodeSkeleton.ClassFileKey) {
    data.put(ByteCodeSkeleton.ClassMethodsKey, methods.toBuffer)
    data.put(ClassPackage, _package)
    data.put(ClassName, name)
    data.put(ClassImports, imports)
    mbParent match {
      case Some(parent) => data.put(ClassParent, parent)
      case _ =>
    }
  }

  def getParent(clazz: MetaObject): Option[String] = clazz.data.get(ClassParent).map(a => a.asInstanceOf[String])

  def getClassName(clazz: MetaObject) = clazz(ClassName).asInstanceOf[String]

  def getMethods(clazz: MetaObject) = clazz(ByteCodeSkeleton.ClassMethodsKey).asInstanceOf[mutable.Buffer[MetaObject]]

  object ClassPackage

  object ClassImports

  object ClassParent

  object ClassName

}
