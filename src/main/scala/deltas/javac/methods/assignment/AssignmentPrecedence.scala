package deltas.javac.methods.assignment

import core.deltas.grammars.LanguageGrammars
import core.deltas.node.GrammarKey
import core.deltas.{Contract, DeltaWithGrammar, Language}
import deltas.javac.expressions.ExpressionSkeleton

object AssignmentPrecedence extends DeltaWithGrammar {

  override def dependencies: Set[Contract] = Set(ExpressionSkeleton)

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    val expressionGrammar = grammars.find(ExpressionSkeleton.ExpressionGrammar)
    val assignmentGrammar = grammars.create(AssignmentGrammar, expressionGrammar.inner)
    expressionGrammar.inner = assignmentGrammar
  }

  object AssignmentGrammar extends GrammarKey

  override def description: String = "Wraps the current expression grammar in a failing assignment grammar."
}