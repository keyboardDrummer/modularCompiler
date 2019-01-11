package deltas.javac.methods.assignment

import core.deltas.grammars.LanguageGrammars
import core.deltas.path.{NodePath, PathRoot}
import core.deltas.{Contract, DeltaWithGrammar, DeltaWithPhase}
import core.language.node.{Node, NodeShape}
import core.language.{Compilation, Language}
import deltas.expression.additive.AdditionDelta

trait OperatorWithAssignmentDelta extends DeltaWithPhase with DeltaWithGrammar {

  override def dependencies: Set[Contract] = Set(SimpleAssignmentDelta)

  val shape: NodeShape

  def neww(target: Node, value: Node) =
    new Node(shape, SimpleAssignmentDelta.Target -> target, SimpleAssignmentDelta.Value -> value)

  def keyword: String
  def operatorShape: NodeShape

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    import grammars._

    val assignmentGrammar = find(AssignmentPrecedence.AssignmentGrammar)
    val assignmentTarget = find(SimpleAssignmentDelta.Target)
    val operatorGrammar = assignmentTarget ~~
      (keyword ~~> assignmentGrammar.as(SimpleAssignmentDelta.Value)) asNode shape
    assignmentGrammar.addAlternative(operatorGrammar)
  }

  def transformAssignment(incrementAssignment: NodePath, state: Language): Unit = {
    val target = SimpleAssignmentDelta.getTarget(incrementAssignment)
    val newValue = operatorShape.createWithSource(
      AdditionDelta.Left -> incrementAssignment.current(SimpleAssignmentDelta.Target),
      AdditionDelta.Right -> incrementAssignment.getWithSource(SimpleAssignmentDelta.Value))
    val assignment = SimpleAssignmentDelta.neww(target, newValue)
    incrementAssignment.replaceData(assignment)
  }

  override def transformProgram(program: Node, compilation: Compilation): Unit = {
    PathRoot(program).visitShape(shape, obj => transformAssignment(obj, compilation))
  }
}
