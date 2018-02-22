package deltas.javac.classes

import core.deltas._
import core.deltas.path.NodePath
import core.language.{Compilation, Language}
import core.language.node.NodeShape
import core.smarts.ConstraintBuilder
import core.smarts.objects.Declaration
import core.smarts.scopes.objects.Scope
import deltas.javac.classes.skeleton.{JavaClassSkeleton, PackageSignature}
import deltas.javac.methods.{ResolveNamespaceOrObjectVariableAmbiguity, IsNamespaceOrObjectExpression, MemberSelectorDelta, VariableDelta}
import deltas.javac.methods.VariableDelta.Shape

object VariableReferenceKind extends Delta with IsNamespaceOrObjectExpression {
  override def inject(language: Language): Unit = {
    super.inject(language)
    MemberSelectorDelta.getReferenceKindRegistry(language).put(Shape, (compilation, variable) => {
      val compiler = JavaClassSkeleton.getClassCompiler(compilation)
      getReferenceKind(variable, compiler)
    })
  }

  def getReferenceKind(variable: NodePath, classCompiler: ClassCompiler): ReferenceKind = {

    val name = VariableDelta.getVariableName(variable)
    val isClass = classCompiler.classNames.contains(name)
    if (isClass)
      new ClassOrObjectReference(classCompiler.findClass(name), true)
    else {
      val mbPackage = classCompiler.javaCompiler.classPath.content.get(name)
      if (mbPackage.isDefined)
        new PackageReference(mbPackage.get.asInstanceOf[PackageSignature])
      else {
        MemberSelectorDelta.getReferenceKindFromExpressionType(classCompiler, variable)
      }
    }
  }

  override def dependencies: Set[Contract] = Set(VariableDelta, JavaClassSkeleton)

  override def description: String = "Enables recognizing the kind of an identifier, whether is a class, package or object."

  override def getScopeDeclaration(compilation: Compilation, builder: ConstraintBuilder, variable: NodePath, scope: Scope): Declaration = {
    val namespaceOrObjectVariableDeclaration =
      builder.resolve(VariableDelta.getVariableName(variable), variable.getLocation(VariableDelta.Name), scope)
    val result = builder.declarationVariable()
    builder.add(ResolveNamespaceOrObjectVariableAmbiguity(namespaceOrObjectVariableDeclaration, result))
    result
  }

  override def shape: NodeShape = Shape
}
