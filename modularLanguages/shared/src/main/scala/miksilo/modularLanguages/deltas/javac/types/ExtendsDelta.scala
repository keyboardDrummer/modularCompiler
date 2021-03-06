package miksilo.modularLanguages.deltas.javac.types

import miksilo.modularLanguages.core.deltas.grammars.LanguageGrammars
import miksilo.modularLanguages.core.node.{NodeField, NodeLike, NodeShape}
import miksilo.modularLanguages.core.deltas.{Contract, DeltaWithGrammar, HasShape}
import miksilo.languageServer.core.language.{Compilation, Language}
import miksilo.languageServer.core.smarts.ConstraintBuilder
import miksilo.languageServer.core.smarts.scopes.objects.Scope
import miksilo.languageServer.core.smarts.types.objects.Type
import miksilo.modularLanguages.deltas.bytecode.types.{HasTypeDelta, TypeSkeleton}

object ExtendsDelta extends DeltaWithGrammar with HasShape with HasTypeDelta {

  override def description: String = "Adds the 'extends' type function. Example: 'T extends U'."

  object Shape extends NodeShape
  object ExtendsBody extends NodeField
  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    import grammars._
    val byteCodeArgumentGrammar = find(TypeApplicationDelta.ByteCodeTypeArgumentGrammar)
    byteCodeArgumentGrammar.addAlternative(("+" ~~> byteCodeArgumentGrammar.as(ExtendsBody)).asNode(Shape))

    val javaTypeGrammar = find(TypeSkeleton.JavaTypeGrammar)
    val javaArgumentGrammar = find(TypeApplicationDelta.JavaTypeArgumentGrammar)
    javaArgumentGrammar.addAlternative(("?" ~~> "extends" ~~> javaTypeGrammar.as(ExtendsBody)).asNode(Shape))
  }

  val shape: NodeShape = Shape

  override def getType(compilation: Compilation, builder: ConstraintBuilder, path: NodeLike, parentScope: Scope): Type = {
    builder.typeVariable() //TODO add generics.
  }

  override def dependencies: Set[Contract] = Set(TypeApplicationDelta)
}
