package deltas.javac.types

import core.bigrammar.BiGrammar
import core.bigrammar.grammars.Keyword
import core.deltas.Compilation
import core.deltas.grammars.LanguageGrammars
import core.deltas.node.{Node, NodeLike, NodeShape}
import core.language.Language
import core.smarts.ConstraintBuilder
import core.smarts.scopes.objects.Scope
import core.smarts.types.objects.{PrimitiveType, Type}
import deltas.bytecode.types.{ByteCodeTypeInstance, IntTypeDelta, StackType}

object BooleanTypeDelta extends ByteCodeTypeInstance
  with StackType //TODO remove this and change VariablePool accordingly.
{
  val constraintType: Type = PrimitiveType("Boolean")

  override val shape = BooleanTypeKey

  override def getSuperTypes(_type: Node, state: Language): Seq[Node] = Seq.empty

  override def getByteCodeGrammar(grammars: LanguageGrammars): BiGrammar = {
    import grammars._
    Keyword("Z", false) ~> value(booleanType)
  }

  override def getStackType(_type: Node, language: Language): Node = IntTypeDelta.intType

  override def getJavaGrammar(grammars: LanguageGrammars) = {
    import grammars._
    "boolean" ~> value(booleanType)
  }

  def booleanType = new Node(BooleanTypeKey)

  object BooleanTypeKey extends NodeShape

  override def description: String = "Defines the boolean type."

  override def getStackSize: Int = IntTypeDelta.getStackSize

  override def getType(compilation: Compilation, builder: ConstraintBuilder, _type: NodeLike, parentScope: Scope): Type = constraintType
}
