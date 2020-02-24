package deltas

import core.deltas.ShapeProperty
import core.deltas.path.NodePath
import core.language.{Compilation, SourceElement}
import core.smarts.ConstraintBuilder
import core.smarts.scopes.objects.Scope
import deltas.javac.classes.skeleton.{HasConstraints, HasDeclaration}

object ConstraintSkeleton {

  val hasDeclarations: ShapeProperty[HasDeclaration] = new ShapeProperty[HasDeclaration]
  val hasConstraints: ShapeProperty[HasConstraints] = new ShapeProperty[HasConstraints]

  def constraints(compilation: Compilation, builder: ConstraintBuilder, path: NodePath, parentScope: Scope): Unit = {
    hasConstraints(compilation, path.shape).collectConstraints(compilation, builder, path, parentScope)
  }
}