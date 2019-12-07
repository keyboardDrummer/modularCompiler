package core.deltas

import java.io.InputStream

import core.bigrammar.BiGrammarToParser
import core.bigrammar.BiGrammarToParser.{Reader, _}
import core.deltas.grammars.LanguageGrammars
import core.deltas.path.PathRoot
import core.language.node.Node
import core.language.{Compilation, Language}
import core.parsers.editorParsers.{SingleParseResult, StopFunction, TimeRatioStopFunction}
import core.parsers.sequences.SingleResultParser
import jsonRpc.LazyLogging
import util.StreamUtils

import scala.io.Source

case class ParseUsingTextualGrammar(stopFunction: StopFunction = new TimeRatioStopFunction)
  extends DeltaWithPhase with LazyLogging {

  override def transformProgram(program: Node, compilation: Compilation): Unit = {
    val phase = Language.getParsePhaseFromParser[Node, Input](input => new Reader(input), (program, uri) => {
      program.startOfUri = Some(uri)
      PathRoot(program)
    }, parserProp.get(compilation), stopFunction)
    phase.action(compilation)
  }

  def parseStream[T](compilation: Compilation, parser: SingleResultParser[T, BiGrammarToParser.Input], input: String):
    SingleParseResult[T, BiGrammarToParser.Input] = {
    parser.parse(new Reader(input), stopFunction, compilation.metrics)
  }

  val parserProp = new Property[SingleResultParser[Node, BiGrammarToParser.Input]](null)

  override def inject(language: Language): Unit = {
    super.inject(language)
    parserProp.add(language, toParserBuilder(LanguageGrammars.grammars.get(language).root).map(r => r.asInstanceOf[Node]).getWholeInputParser)
  }

  override def description: String = "Parses the input file using a textual grammar."

  override def dependencies: Set[Contract] = Set.empty
}

