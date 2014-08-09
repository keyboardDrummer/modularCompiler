package transformations.javac.statements

import core.grammar.seqr
import core.transformation.grammars.GrammarCatalogue
import core.transformation.{Contract, MetaObject, TransformationState}
import transformations.bytecode.LabelledJumps
import transformations.bytecode.simpleBytecode.InferredStackFrames
import transformations.javac.expressions.ExpressionC

object WhileC extends StatementInstance {

  object WhileKey

  object WhileCondition

  object WhileBody

  override val key: AnyRef = WhileKey

  def getCondition(_while: MetaObject) = _while(WhileCondition).asInstanceOf[MetaObject]

  def getBody(_while: MetaObject) = _while(WhileBody).asInstanceOf[Seq[MetaObject]]

  override def toByteCode(_while: MetaObject, state: TransformationState): Seq[MetaObject] = {
    val conditionInstructions = ExpressionC.getToInstructions(state)(getCondition(_while))
    val body = getBody(_while)
    val bodyInstructions = body.flatMap(statement => StatementC.getToInstructions(state)(statement))

    val startLabel = state.getUniqueLabel("start")
    val endLabel = state.getUniqueLabel("end")
    Seq(InferredStackFrames.label(startLabel)) ++
      conditionInstructions ++
      Seq(LabelledJumps.ifZero(endLabel)) ++
      bodyInstructions ++
      Seq(LabelledJumps.goTo(startLabel), InferredStackFrames.label(endLabel))
  }


  override def dependencies: Set[Contract] = super.dependencies ++ Set(BlockC)

  override def transformGrammars(grammars: GrammarCatalogue): Unit = {
    val statementGrammar = grammars.find(StatementC.StatementGrammar)
    val expressionGrammar = grammars.find(ExpressionC.ExpressionGrammar)
    val blockGrammar = grammars.find(BlockC.BlockGrammar)
    val whileGrammar = "while" ~> ("(" ~> expressionGrammar <~ ")") ~ blockGrammar ^^ { case condition seqr body => new MetaObject(WhileKey, WhileCondition -> condition, WhileBody -> body)}
    statementGrammar.inner = statementGrammar.inner | whileGrammar
  }
}
