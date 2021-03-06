package miksilo.modularLanguages.deltas.json

import miksilo.modularLanguages.core.bigrammar.{BiGrammar, BiGrammarWriter}
import miksilo.modularLanguages.core.bigrammar.grammars.Colorize
import miksilo.modularLanguages.core.deltas.grammars.LanguageGrammars
import miksilo.modularLanguages.core.deltas.path.NodePath
import miksilo.modularLanguages.core.deltas.{Contract, DeltaWithGrammar}
import miksilo.modularLanguages.core.node.{Node, NodeField, NodeShape}
import miksilo.languageServer.core.language.{Compilation, Language}
import miksilo.editorParser.parsers.editorParsers.{OffsetPointerRange, Position, SourceRange}
import miksilo.languageServer.core.smarts.ConstraintBuilder
import miksilo.languageServer.core.smarts.scopes.objects.Scope
import miksilo.languageServer.core.smarts.types.objects.{PrimitiveType, Type}
import miksilo.modularLanguages.deltas.expression.StringLiteralDelta.Value
import miksilo.modularLanguages.deltas.expression.{ExpressionDelta, ExpressionInstance, StringLiteralDelta}

import scala.util.matching.Regex

object JsonStringLiteralDelta extends DeltaWithGrammar with ExpressionInstance {

  override def description: String = "Adds the double quoted string literal"

  import StringLiteralDelta.Shape
  val shape = Shape

  override def dependencies: Set[Contract] = Set(ExpressionDelta)

  val stringInnerRegex: Regex = """"([^"\x00-\x1F\x7F\\]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*""".r

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    val inner = {
      import miksilo.modularLanguages.core.bigrammar.DefaultBiGrammarWriter._
      dropPrefix(grammars, grammars.regexGrammar(stringInnerRegex, "string literal"), Value, "\"") ~<
        BiGrammarWriter.stringToGrammar("\"")
    }
    import grammars._
    val grammar = Colorize(inner, "string.quoted.double")
    find(ExpressionDelta.FirstPrecedenceGrammar).addAlternative(grammar.asLabelledNode(Shape))
  }

  def dropPrefix(grammars: LanguageGrammars, regex: BiGrammar, field: NodeField, prefix: String) = {
    import grammars._
    regex.map[String, String](r => r.substring(prefix.length), s => { prefix + s }).
      as(field, (from, until) => OffsetPointerRange(from.drop(prefix.length), until))
  }

  override def constraints(compilation: Compilation, builder: ConstraintBuilder, expression: NodePath, _type: Type, parentScope: Scope): Unit = {
    builder.typesAreEqual(_type, PrimitiveType("String"))
  }
}


