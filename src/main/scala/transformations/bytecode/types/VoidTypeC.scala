package transformations.bytecode.types

import core.bigrammar.{BiGrammar, Keyword}
import core.particles.Language
import core.particles.grammars.GrammarCatalogue
import core.particles.node.{Node, NodeClass}

object VoidTypeC extends TypeInstance with StackType {

  override val key = VoidTypeKey

  override def getSuperTypes(_type: Node, state: Language): Seq[Node] = ???

  override def getByteCodeGrammar(grammars: GrammarCatalogue): BiGrammar = Keyword("V", false) ~> produce(voidType)

  override def getStackSize: Int = 0

  override def getJavaGrammar(grammars: GrammarCatalogue) = {
    "void" ~> produce(voidType)
  }

  def voidType = new Node(VoidTypeKey)

  object VoidTypeKey extends NodeClass

  override def description: String = "Defines the void type."
}
