package deltas.javac.statements

import core.deltas._
import core.deltas.grammars.LanguageGrammars
import core.deltas.node._
import core.deltas.path.{Path, SequenceElement}
import deltas.bytecode.ByteCodeMethodInfo
import deltas.bytecode.simpleBytecode.{InferredStackFrames, LabelDelta, LabelledLocations}
import deltas.javac.expressions.ExpressionSkeleton

object WhileDelta extends StatementInstance with WithCompilationState {

  override def description: String = "Enables using the while construct."

  override def toByteCode(_while: Path, compilation: Compilation): Seq[Node] = {
    val methodInfo = _while.findAncestorClass(ByteCodeMethodInfo.MethodInfoKey)
    val startLabel = LabelDelta.getUniqueLabel("start", methodInfo)
    val endLabel = LabelDelta.getUniqueLabel("end", methodInfo)

    val conditionInstructions = ExpressionSkeleton.getToInstructions(compilation)(_while.condition)
    getState(compilation).whileStartLabels += _while -> startLabel

    val body = _while.body
    val bodyInstructions = body.flatMap(statement => StatementSkeleton.getToInstructions(compilation)(statement))

    Seq(InferredStackFrames.label(startLabel)) ++
      conditionInstructions ++
      Seq(LabelledLocations.ifZero(endLabel)) ++
      bodyInstructions ++ Seq(LabelledLocations.goTo(startLabel), InferredStackFrames.label(endLabel))
  }

  override def dependencies: Set[Contract] = super.dependencies ++ Set(BlockDelta)

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    import grammars._
    val statementGrammar = find(StatementSkeleton.StatementGrammar)
    val expression = find(ExpressionSkeleton.ExpressionGrammar)
    val blockGrammar = find(BlockDelta.Grammar)
    val whileGrammar =
      "while" ~> expression.inParenthesis.as(Condition) %
      blockGrammar.as(Body) asLabelledNode WhileKey
    statementGrammar.addOption(whileGrammar)
  }

  def startKey(_while: NodeLike) = (_while,"start")
  def endKey(_while: NodeLike) = (_while,"end")
  override def getNextStatements(obj: Path, labels: Map[Any, Path]): Set[Path] = {
    super.getNextStatements(obj, labels) ++ obj.body.take(1)
  }

  override def getLabels(_whilePath: Path): Map[Any, Path] = {
    val _while = _whilePath.asInstanceOf[SequenceElement]
    val current = _while.current
    val next = _while.next
    var result: Map[Any,Path] = Map(startKey(current) -> _while, endKey(current) -> next)
    val body = _whilePath.body
    if (body.nonEmpty)
      result += getNextLabel(body.last) -> next
    result
  }

  class State {
    var whileStartLabels: Map[Path, String] = Map.empty
  }

  override def createState = new State()

  implicit class While[T <: NodeLike](val node: T) extends NodeWrapper[T] {
    def condition: T = node(Condition).asInstanceOf[T]
    def body: Seq[T] = node(Body).asInstanceOf[Seq[T]]
  }

  def create(condition: Node, body: Seq[Node]) = new Node(WhileKey, Condition -> condition, Body -> body)

  override val key = WhileKey

  object WhileKey extends NodeClass

  object Condition extends NodeField

  object Body extends NodeField
}
