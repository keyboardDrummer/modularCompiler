package deltas.statement

import core.deltas._
import core.deltas.grammars.LanguageGrammars
import core.deltas.path.{NodePath, NodeSequenceElement, PathRoot}
import core.language.node.{Node, NodeGrammar, NodeShape}
import core.language.{Compilation, Language}

import scala.collection.mutable

/*
For each while loop containing a continue, as start label is placed before the while loop, and the continue's are translated to go-to statements that target the start label.
 */
object WhileContinueDelta extends DeltaWithPhase with DeltaWithGrammar {

  override def description: String = "Moves the control flow to the start of the while loop."

  override def dependencies: Set[Contract] = Set(WhileLoopDelta, LabelStatementDelta)

  def transformProgram(program: Node, compilation: Compilation): Unit = {
    val startLabels = new mutable.HashMap[NodePath, String]()
    PathRoot.fromCompilation(compilation).visitShape(ContinueKey, path => transformContinue(compilation, path, startLabels))
  }

  def transformContinue(compilation: Compilation, continuePath: NodePath, startLabels: mutable.Map[NodePath, String]): Unit = {
    val containingWhile = continuePath.findAncestorShape(WhileLoopDelta.Shape)
    val label = startLabels.getOrElseUpdate(containingWhile, addStartLabel(compilation, containingWhile))
    continuePath.replaceData(GotoStatementDelta.neww(label))
  }

  def addStartLabel(compilation: Compilation, whilePath: NodePath): String = {
    val startLabel = LabelStatementDelta.getUniqueLabel(compilation, "whileStart", whilePath)
    whilePath.asInstanceOf[NodeSequenceElement].replaceWith(Seq(LabelStatementDelta.neww(startLabel), whilePath.current))
    startLabel
  }

  override def transformGrammars(grammars: LanguageGrammars, language: Language): Unit = {
    import grammars._
    val statementGrammar = find(StatementDelta.Grammar)
    statementGrammar.addAlternative(new NodeGrammar("continue" ~ ";", ContinueKey))
  }

  object ContinueKey extends NodeShape
  def continue = new Node(ContinueKey)
}

