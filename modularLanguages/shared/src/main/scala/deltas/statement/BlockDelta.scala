package deltas.statement

import core.deltas._
import core.deltas.grammars.LanguageGrammars
import core.deltas.path.NodePath
import core.document.BlankLine
import core.language.node._
import core.language.{Compilation, Language}
import core.smarts.ConstraintBuilder
import core.smarts.scopes.objects.Scope
import deltas.ConstraintSkeleton
import deltas.javac.expressions.{ConvertsToByteCodeDelta, ToByteCodeSkeleton}
import deltas.statement.BlockDelta.BlockStatement

object BlockDelta extends DeltaWithGrammar with StatementInstance {

  override def description: String = "Defines a grammar for blocks."
  override def dependencies: Set[Contract] = Set(StatementDelta)

  object Shape extends NodeShape
  object Statements extends NodeField

  def neww(statements: Seq[Node] = Seq.empty): Node = Shape.create(Statements -> statements)

  implicit class BlockStatement[T <: NodeLike](val node: T) extends NodeWrapper[T] {
    def statements: Seq[T] = node(Statements).asInstanceOf[Seq[T]]
    def statements_=(value: Seq[T]): Unit = node(Statements) = value
  }

  object BlockOrStatementGrammar extends GrammarKey
  object StatementAsBlockGrammar extends GrammarKey
  object BlockGrammar extends GrammarKey

  val indentAmount = 4
  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    import grammars._
    val statementGrammar = find(StatementDelta.Grammar)

    //create(BlockGrammar, statementGrammar.manySeparatedVertical("").as(Statements) asNode Shape) //
    val blockGrammar = create(BlockGrammar, "{" %> statementGrammar.manyVertical.as(Statements).indent(indentAmount) %< "}" asNode Shape)
    val statementAsSequence = statementGrammar.map[Any, Seq[Any]](statement => Seq(statement), x => x.head)
    val statementAsBlockGrammar = create(StatementAsBlockGrammar, statementAsSequence.as(Statements).asNode(Shape))
    create(BlockOrStatementGrammar, blockGrammar | statementAsBlockGrammar)
  }

  def collectConstraints(compilation: Compilation, builder: ConstraintBuilder, statements: Seq[NodePath], parentScope: Scope): Unit = {
    for(statement <- statements) {
      ConstraintSkeleton.constraints(compilation, builder, statement, parentScope)
    }
  }

  override def shape: NodeShape = Shape

  override def collectConstraints(compilation: Compilation, builder: ConstraintBuilder, statement: NodePath, parentScope: Scope): Unit = {
    val block: BlockStatement[NodePath] = statement
    val blockScope = builder.newScope(parentScope, "blockScope")
    block.statements.foreach(childStatement => ConstraintSkeleton.constraints(compilation, builder, childStatement, blockScope))
  }

  override def getControlFlowGraph(language: Language, statement: NodePath, labels: Map[Any, NodePath]): ControlFlowGraph = {
    val block: BlockStatement[NodePath] = statement
    val childGraphs = block.statements.map(statement => ControlFlowGraph.getControlFlowGraph(language, statement, labels))
    childGraphs.fold[ControlFlowGraph](ControlFlowGraph.empty)((l,r) => l.sequence(r))
  }
}