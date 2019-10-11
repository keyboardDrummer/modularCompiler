package deltas

import core.bigrammar.BiGrammar
import core.bigrammar.printer.{BiGrammarToPrinter, PrintError}
import core.bigrammar.textMate.BiGrammarToTextMate.jsonLanguage
import core.deltas.grammars.LanguageGrammars
import core.deltas.path.PathRoot
import core.deltas.{Contract, Delta}
import core.language.{Language, Phase}
import core.responsiveDocument.ResponsiveDocument

import scala.util.Try

case class PrettyPrint(recover: Boolean = false) extends Delta
{
  override def inject(language: Language): Unit = {
    val foundGrammar = LanguageGrammars.grammars.get(language).root
    language.data(this) = foundGrammar.deepClone

    language.compilerPhases = List(Phase(this, description, compilation => {
      val grammar = getOutputGrammar(language)
      val documentTry: Try[ResponsiveDocument] = Try(BiGrammarToPrinter.toDocument(compilation.program.asInstanceOf[PathRoot].current, grammar))
      val documentTryWithOptionalRecover: Try[ResponsiveDocument] = if (recover) {
        documentTry.recover({ case e: PrintError => e.toDocumentWithPartial })
      }
      else {
        documentTry
      }
      val document: ResponsiveDocument = documentTryWithOptionalRecover.get
      compilation.output = document.renderString()
    }))
  }

  override def description: String = "Prints the program by generating a pretty printer from its grammar."

  def getOutputGrammar(language: Language): BiGrammar = language.data(this).asInstanceOf[BiGrammar]

  override def dependencies: Set[Contract] = Set.empty
}