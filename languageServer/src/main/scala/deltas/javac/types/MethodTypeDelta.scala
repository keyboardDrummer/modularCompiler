package deltas.javac.types

import core.bigrammar.BiGrammar
import core.bigrammar.grammars.BiFailure
import core.deltas.grammars.LanguageGrammars
import core.language.Compilation
import core.language.node._
import core.smarts.ConstraintBuilder
import core.smarts.scopes.objects.Scope
import core.smarts.types.objects._
import deltas.bytecode.types.{ByteCodeTypeInstance, TypeSkeleton}

object MethodTypeDelta extends ByteCodeTypeInstance {

  implicit class MethodType[T <: NodeLike](node: T) {
    def returnType: T = node(ReturnType).asInstanceOf[T]
    def returnType_=(value: T): Unit = node(ReturnType) = value

    def parameterTypes: Seq[T] = node(Parameters).asInstanceOf[Seq[T]]
    def parameterTypes_=(value: Seq[T]): Unit = node(Parameters) = value
  }

  def neww  (returnType: Node, parameterTypes: Seq[Node]) = {
    new Node(Shape,
      Parameters -> parameterTypes,
      ReturnType -> returnType,
      ThrowsSignature -> Seq.empty[Node])
  }

  object Shape extends NodeShape

  object Parameters extends NodeField

  object ReturnType extends NodeField

  object ThrowsSignature extends NodeField

  val shape = Shape

  override def description: String = "Defines the method type."

  override def getSuperTypes(_type: Node): Seq[Node] = ???

  override def getJavaGrammar(grammars: LanguageGrammars): BiGrammar = BiFailure()

  override def getByteCodeGrammar(grammars: LanguageGrammars): BiGrammar = {
    import grammars._
    val typeGrammar = find(TypeSkeleton.ByteCodeTypeGrammar)
    val throwsGrammar = ("^" ~> typeGrammar)*
    val methodGrammar = (("(" ~> (typeGrammar*).as(Parameters) ~< ")") ~ typeGrammar.as(ReturnType) ~ throwsGrammar.as(ThrowsSignature)).asNode(Shape)
    methodGrammar
  }

  override def getType(compilation: Compilation, builder: ConstraintBuilder, _type: NodeLike, parentScope: Scope): Type = {
    val parameters = _type.parameterTypes
    val returnTypeNode = _type.returnType
    getType(compilation, builder, parentScope, parameters, returnTypeNode)
  }

  def getType(compilation: Compilation, builder: ConstraintBuilder, parentScope: Scope, parameters: Seq[NodeLike], returnTypeNode: NodeLike): Type = {
    val parameterTypes = parameters.map(parameter => TypeSkeleton.getType(compilation, builder, parameter, parentScope))
    val returnType = TypeSkeleton.getType(compilation, builder, returnTypeNode, parentScope)
    FunctionType.curry(parameterTypes, returnType)
  }

  override def constraintName = FuncPrimitive.name

  override def fromConstraintType(compilation: Compilation, _type: Type): Node = {
    def uncurry(_type: Type): List[Type] = _type match {
      case TypeApplication(FuncPrimitive, twoArguments, _) =>
        val argument = twoArguments.head
        val result = twoArguments.tail.head
        argument :: uncurry(result)
      case result => List(result)
    }
    val arguments = uncurry(_type).map(t => compilation.proofs.resolveType(t))
    val parameterTypes = arguments.dropRight(1).map(t => TypeSkeleton.fromConstraintType(compilation, t))
    neww(TypeSkeleton.fromConstraintType(compilation, arguments.last), parameterTypes)
  }
}