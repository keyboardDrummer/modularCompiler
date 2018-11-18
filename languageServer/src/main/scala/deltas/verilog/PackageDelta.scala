package deltas.verilog

import core.deltas.grammars.LanguageGrammars
import core.deltas.path.NodePath
import core.deltas.{Contract, DeltaWithGrammar}
import core.document.BlankLine
import core.language.{Compilation, Language}
import core.language.node.{NodeField, NodeLike, NodeShape, NodeWrapper}
import core.smarts.ConstraintBuilder
import core.smarts.scopes.objects.Scope
import deltas.ConstraintSkeleton
import deltas.javac.classes.skeleton.HasConstraintsDelta

object PackageDelta extends DeltaWithGrammar with HasConstraintsDelta {

  object Shape extends NodeShape
  object Name extends NodeField
  object Members extends NodeField

  override def transformGrammars(grammars: LanguageGrammars, language: Language): Unit = {
    import grammars._
    val fileMember = find(VerilogFileDelta.Members)

    val packageMember = create(Members)
    val _package = "package" ~~ identifier.as(Name) ~ ";" %
      packageMember.manySeparatedVertical(BlankLine).as(Members) %
      "endpackage" ~~ (":" ~~ identifier).option
    fileMember.addAlternative(_package asNode Shape)

    val clazz = find(VerilogClassDelta.Shape)
    packageMember.addAlternative(clazz)
  }

  implicit class Package[T <: NodeLike](val node: T) extends NodeWrapper[T] {
    def name: String = node.getValue(Name).asInstanceOf[String]
    def members: Seq[T] = node(Members).asInstanceOf[Seq[T]]
  }

  override def description: String = "Adds packages to Verilog"

  override def dependencies: Set[Contract] = Set(VerilogFileDelta, VerilogClassDelta)

  override def shape: NodeShape = Shape

  override def collectConstraints(compilation: Compilation, builder: ConstraintBuilder, path: NodePath, parentScope: Scope): Unit = {
    val _package: Package[NodePath] = path
    val packageDeclaration = builder.declare(_package.name, parentScope, path.getMember(Name))
    val packageScope = builder.declareScope(packageDeclaration, debugName = "package")

    for(member <- _package.members) {
      ConstraintSkeleton.constraints(compilation, builder, member, packageScope)
    }
  }
}
