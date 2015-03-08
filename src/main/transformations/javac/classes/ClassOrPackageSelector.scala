package transformations.javac.classes

import core.transformation.sillyCodePieces.Particle
import core.transformation.{Contract, MetaObject, TransformationState}
import SelectorC.SelectorKey

object ClassOrPackageSelector extends Particle {
  override def dependencies: Set[Contract] = Set(SelectorC, JavaClassSkeleton)

  override def inject(state: TransformationState): Unit = {
    JavaClassSkeleton.getReferenceKindRegistry(state).put(SelectorKey, selector => {
      val compiler = JavaClassSkeleton.getClassCompiler(state)
      getReferenceKind(selector, compiler)
    })
  }

  def getReferenceKind(selector: MetaObject, compiler: ClassCompiler): ReferenceKind = {
    val obj = SelectorC.getSelectorObject(selector)
    val member = SelectorC.getSelectorMember(selector)
    compiler.getReferenceKind(obj) match {
      case PackageReference(info) => info.content(member) match {
        case result: PackageInfo => new PackageReference(result)
        case result: ClassInfo => new ClassOrObjectReference(result, true)
      }
      case ClassOrObjectReference(info, _) =>
        val field = info.getField(member)
        val fieldClassType = compiler.findClass(field._type)
        new ClassOrObjectReference(fieldClassType, false)
    }
  }

  override def description: String = "Enables recognizing the kind of a selection, whether is a class, package or object."
}
