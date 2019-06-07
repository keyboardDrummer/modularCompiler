package deltas.smithy

import core.bigrammar.BiGrammar
import core.deltas.DeltaWithGrammar
import core.deltas.grammars.LanguageGrammars
import core.deltas.path.NodePath
import core.language.node.{NodeField, NodeLike, NodeShape, NodeWrapper}
import core.language.{Compilation, Language}
import core.smarts.ConstraintBuilder
import core.smarts.scopes.objects.Scope
import deltas.javac.classes.skeleton.HasConstraintsDelta
import deltas.json.JsonObjectLiteralDelta.ObjectLiteral
import deltas.smithy.ServiceDelta.{Body}
import deltas.{ConstraintSkeleton, FileWithMembersDelta, HasNameDelta}

object StructureStatementDelta extends DeltaWithGrammar with HasConstraintsDelta {

  object Shape extends NodeShape
  object Members extends NodeField

  object MemberShape extends NodeShape
  object MemberType extends NodeField

  override def transformGrammars(grammars: LanguageGrammars, language: Language): Unit = {
    import grammars._

    val name = find(HasNameDelta.Name)
    val shapeIdentifier = find(RelativeShapeIdentifierDelta.ShapeIdentifierGrammar)
    val traits = find(TraitDelta.Traits)
    val structuredMember: BiGrammar = traits.option % name ~ ":" ~~ shapeIdentifier.as(MemberType) asNode MemberShape
    val trailingComma = ",".option
    val structureBody = name ~~ (structuredMember.manySeparatedVertical(",").as(Members) ~ trailingComma).inBraces
    val grammar = "structure" ~~ structureBody asNode Shape
    val members = find(ShapeStatementDelta.ShapeBody)
    members.addAlternative(grammar)
  }

  override def description = "Adds structure statement delta"

  override def dependencies = Set(FileWithMembersDelta)

  override def shape = Shape

  implicit class Structure[T <: NodeLike](val node: T) extends NodeWrapper[T] {
    def members: Seq[StructureMember[T]] = NodeWrapper.wrapList(node(Members).asInstanceOf[Seq[T]])
  }

  implicit class StructureMember[T <: NodeLike](val node: T) extends HasNameDelta.HasName[T] {
    def _type: T = node(MemberType).asInstanceOf[T]
  }

  override def collectConstraints(compilation: Compilation, builder: ConstraintBuilder,
                                  path: NodePath, parentScope: Scope): Unit = {
    builder.declareSourceElement(path.getSourceElement(HasNameDelta.Name), parentScope,
      Some(RelativeShapeIdentifierDelta.shapeType))

    val structure: Structure[NodePath] = path
    structure.members.foreach(member => {
      // TODO get the resolved declaration from the type, and use it to build a scope for the structure.
      ConstraintSkeleton.constraints(compilation, builder, member._type, parentScope)
    })
  }
}
