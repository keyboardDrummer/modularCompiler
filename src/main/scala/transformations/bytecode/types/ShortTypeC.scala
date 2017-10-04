package transformations.bytecode.types

import core.bigrammar.{BiGrammar, Keyword}
import core.particles.Language
import core.particles.grammars.GrammarCatalogue
import core.particles.node.{Node, NodeClass}

object ShortTypeC extends TypeInstance with StackType {

  override val key = ShortTypeKey

  override def getSuperTypes(_type: Node, state: Language): Seq[Node] = Seq.empty //TODO extend. long ?


  override def getByteCodeGrammar(grammars: GrammarCatalogue): BiGrammar = new Keyword("S",false) ~> produce(shortType)

  override def getJavaGrammar(grammars: GrammarCatalogue) = {
    "short" ~> produce(shortType)
  }

  def shortType = new Node(ShortTypeKey)

  override def getStackSize: Int = 1

  object ShortTypeKey extends NodeClass

  override def description: String = "Defines the short type."
}
