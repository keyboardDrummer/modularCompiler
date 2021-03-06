package miksilo.modularLanguages.deltas.bytecode.attributes

import miksilo.modularLanguages.core.deltas.grammars.LanguageGrammars
import miksilo.modularLanguages.core.deltas.{Contract, DeltaWithGrammar}
import miksilo.languageServer.core.language.Language
import miksilo.modularLanguages.core.node.{Node, NodeField, NodeShape}
import miksilo.modularLanguages.deltas.bytecode.ByteCodeSkeleton

object UnParsedAttribute extends DeltaWithGrammar {

  override def description: String = "An attribute whose data has not been parsed yet"

  override def dependencies: Set[Contract] = Set[Contract](ByteCodeSkeleton)

  class UnParsedAttribute(val node: Node) {
    def nameIndex: Int = node(UnParsedAttribute.Name).asInstanceOf[Int]
    def nameIndex_=(value: Int): Unit = node(UnParsedAttribute.Name) = value

    def data = node(UnParsedAttribute.Data).asInstanceOf[Seq[Byte]]
    def data_=(value: Seq[Byte]): Unit = node(UnParsedAttribute.Data) = value
  }

  def construct(nameIndex: Int, bytes: Seq[Byte]) = new Node(Shape, Name -> nameIndex, Data -> bytes)

  object Shape extends NodeShape
  object Name extends NodeField
  object Data extends NodeField

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    import grammars._
    val grammar = "UnParsedAttribute" ~ "," ~~ "nameIndex" ~ ":" ~~> integer.as(Name) asNode Shape
    find(ByteCodeSkeleton.AttributeGrammar).addAlternative(grammar)
  }
}
