package deltas.json

import core.deltas.{Delta, LanguageFromDeltas}
import deltas.expression.{DefaultExpressionDelta, IntLiteralDelta}
import deltas.expressions.ExpressionDelta
import deltas.javac.expressions.ExpressionLanguageDelta
import deltas.javac.expressions.literals.BooleanLiteralDelta

object JsonLanguage {
  val deltas: Seq[Delta] = Seq[Delta](ExpressionLanguageDelta, DefaultExpressionDelta, BooleanLiteralDelta, JsonObjectLiteralDelta,
    JsonArrayLiteralDelta, JsonStringLiteralDelta, IntLiteralDelta, ExpressionDelta)
  val language = LanguageFromDeltas(deltas)
}
