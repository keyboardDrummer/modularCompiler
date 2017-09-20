package transformations.bytecode.constants

import core.bigrammar.BiGrammar
import core.particles.CompilationState
import core.particles.grammars.GrammarCatalogue
import core.particles.node.{Node, NodeClass, NodeField}
import transformations.bytecode.ByteCodeSkeleton
import transformations.bytecode.PrintByteCode._
import transformations.bytecode.coreInstructions.ConstantPoolIndexGrammar

object NameAndTypeConstant extends ConstantEntry {

  object NameAndTypeKey extends NodeClass

  object NameAndTypeName extends NodeField

  object NameAndTypeType extends NodeField

  def nameAndType(nameIndex: Node, typeIndex: Node): Node = new Node(NameAndTypeKey,
    NameAndTypeName -> nameIndex,
    NameAndTypeType -> typeIndex)

  def nameAndType(nameIndex: Int, typeIndex: Int): Node = new Node(NameAndTypeKey,
    NameAndTypeName -> nameIndex,
    NameAndTypeType -> typeIndex)

  def getName(nameAndType: Node): Int = nameAndType(NameAndTypeName).asInstanceOf[Int]

  def getTypeIndex(nameAndType: Node): Int = nameAndType(NameAndTypeType).asInstanceOf[Int]

  override def key = NameAndTypeKey

  override def getByteCode(constant: Node, state: CompilationState): Seq[Byte] = {
    byteToBytes(12) ++ shortToBytes(getName(constant)) ++
      shortToBytes(getTypeIndex(constant))
  }

  override def inject(state: CompilationState): Unit = {
    super.inject(state)
    ByteCodeSkeleton.getState(state).constantReferences.put(key, Map(
      NameAndTypeName -> Utf8Constant.key,
      NameAndTypeType -> Utf8Constant.key))
  }

  override def getConstantEntryGrammar(grammars: GrammarCatalogue): BiGrammar =
    ("name and type:" ~~> (grammars.find(ConstantPoolIndexGrammar).as(NameAndTypeName) <~ ":") ~
      grammars.find(ConstantPoolIndexGrammar).as(NameAndTypeType)).
    asNode(NameAndTypeKey)

  override def description: String = "Defines the name and type constant, which contains a name and a field or method descriptor."
}