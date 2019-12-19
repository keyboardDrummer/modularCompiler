package application.compilerCockpit

import java.io.InputStream

import core.deltas.{Delta, LanguageFromDeltas}
import core.language.Language
import deltas.PrettyPrint

object PrettyPrintOption extends CompileOption {

  val prettyPrint = PrettyPrint(recover = true)
  var language: Language = _

  override def initialize(sandbox: LanguageSandbox): Unit = {
    val splicedParticles = Delta.replace(sandbox.deltas, MarkOutputGrammar, Seq(prettyPrint))
    language = LanguageFromDeltas(splicedParticles)
  }

  override def run(sandbox: LanguageSandbox, input: String): TextWithGrammar = {
    val compilation = language.compileString(input)
    val outputGrammar = prettyPrint.getOutputGrammar(compilation.language)
    TextWithGrammar(compilation.output, outputGrammar)
  }

  override def name = "Pretty Print"
}