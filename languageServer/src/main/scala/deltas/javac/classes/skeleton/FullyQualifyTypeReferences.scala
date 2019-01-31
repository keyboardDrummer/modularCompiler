package deltas.javac.classes.skeleton

import core.deltas.path.{FieldPath, PathRoot}
import core.deltas.{Contract, Delta}
import core.language.node.Node
import core.language.{Compilation, Language, Phase}
import core.smarts.SolveConstraintsDelta
import deltas.bytecode.types.{QualifiedObjectTypeDelta, UnqualifiedObjectTypeDelta}

object FullyQualifyTypeReferences extends Delta {
  override def description: String = "Replaces unqualified type references with qualified ones."

  override def inject(language: Language): Unit = {
    val phase = Phase(this, compilation => transformProgram(compilation.program, compilation))
    SolveConstraintsDelta.injectPhaseAfterMe(language, phase)
    super.inject(language)
  }

  def transformProgram(program: Node, compilation: Compilation): Unit = {
    PathRoot(program).visitShape(UnqualifiedObjectTypeDelta.Shape, _type => {
      val declaration = compilation.proofs.gotoDefinition(_type).get.origin.get.asInstanceOf[FieldPath].parent.current
      val clazz: JavaClassDelta.JavaClass[Node] = declaration
      val parts = clazz._package ++ Seq(clazz.name)
      _type.replaceData(QualifiedObjectTypeDelta.neww(QualifiedClassName(parts)))
    })
  }

  override def dependencies: Set[Contract] = Set(QualifiedObjectTypeDelta, UnqualifiedObjectTypeDelta)
}
