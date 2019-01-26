package deltas.javac.constructor

import core.deltas.{Contract, DeltaWithPhase}
import core.language.Compilation
import core.language.node.Node
import deltas.javac.statements.ExpressionAsStatementDelta

object ImplicitSuperConstructorCall extends DeltaWithPhase {
  override def dependencies: Set[Contract] = Set(ConstructorDelta)

  override def transformProgram(program: Node, state: Compilation): Unit = {

    for (constructor <- ConstructorDelta.getConstructors(program)) {
      val statements = constructor.body.statements
      var addSuperCall = false
      if (statements.isEmpty)
        addSuperCall = true
      else {
        val firstStatement = statements.head
        if (firstStatement.shape != SuperCallExpressionDelta.SuperCall && firstStatement.shape != ThisCallExpression.ThisCall) {
          addSuperCall = true
        }
      }

      if (addSuperCall)
        constructor.body.statements = Seq(ExpressionAsStatementDelta.create(SuperCallExpressionDelta.superCall())) ++ statements
    }
  }

  override def description: String = "At the start of a constructor body, if no call to a super constructor is present, such a call is added."
}
