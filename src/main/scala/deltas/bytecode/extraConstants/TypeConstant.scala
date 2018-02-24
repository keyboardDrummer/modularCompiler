package deltas.bytecode.extraConstants

import core.bigrammar.BiGrammar
import core.deltas.grammars.LanguageGrammars
import core.language.node.{Node, NodeField, NodeLike, NodeShape}
import core.language.{Compilation, Language}
import deltas.bytecode.ByteCodeFieldInfo.{DescriptorIndex, NameIndex}
import deltas.bytecode.ByteCodeMethodInfo.{MethodDescriptor, Shape, MethodNameIndex}
import deltas.bytecode.constants.MethodTypeConstant.MethodTypeDescriptorIndex
import deltas.bytecode.constants.{ConstantEntry, MethodTypeConstant, NameAndTypeConstant, Utf8ConstantDelta}
import deltas.bytecode.types.TypeSkeleton
import deltas.bytecode.{ByteCodeFieldInfo, ByteCodeMethodInfo, ByteCodeSkeleton, PrintByteCode}

object TypeConstant extends ConstantEntry {
  object Key extends NodeShape
  object Type extends NodeField

  def constructor(_type: Node) = new Node(Key, Type -> _type)

  implicit class TypeConstantWrapper[T <: NodeLike](node: T) {
    def value: T = node(Type).asInstanceOf[T]
    def value_=(value: T): Unit = node(Type) = value
  }

  override def shape = Key

  override def getBytes(compilation: Compilation, constant: Node): Seq[Byte] = {
    val _type: Node = constant(Type).asInstanceOf[Node]
    val typeString = TypeSkeleton.getByteCodeString(compilation)(_type)
    PrintByteCode.toUTF8ConstantEntry(typeString)
  }

  override def inject(language: Language): Unit = {
    super.inject(language)

    ByteCodeSkeleton.constantReferences.add(language, ByteCodeFieldInfo.Shape, Map(
      NameIndex -> Utf8ConstantDelta.shape,
      DescriptorIndex -> TypeConstant.shape))
    ByteCodeSkeleton.constantReferences.add(language, Shape, Map(
      MethodNameIndex -> Utf8ConstantDelta.shape,
      MethodDescriptor -> TypeConstant.shape))
    ByteCodeSkeleton.constantReferences.add(language, MethodTypeConstant.shape, Map(
      MethodTypeDescriptorIndex -> TypeConstant.shape))
    ByteCodeSkeleton.constantReferences.add(language, NameAndTypeConstant.shape, Map(
      NameAndTypeConstant.Name -> Utf8ConstantDelta.shape,
      NameAndTypeConstant.Type -> TypeConstant.shape))
  }

  override def dependencies = Set(MethodTypeConstant, NameAndTypeConstant, ByteCodeMethodInfo, ByteCodeFieldInfo)

  override def getConstantEntryGrammar(grammars: LanguageGrammars): BiGrammar = {
    val typeGrammar = grammars.find(TypeSkeleton.ByteCodeTypeGrammar)
    typeGrammar.as(Type)
  }

  override def description: String = "Adds the field descriptor constant. It contains the type of a field."

  override def getName = "Utf8" //TODO do I want this?
}
