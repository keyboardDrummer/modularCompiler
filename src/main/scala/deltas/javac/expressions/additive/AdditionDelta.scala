package deltas.javac.expressions.additive

import core.deltas._
import core.deltas.grammars.LanguageGrammars
import core.deltas.node._
import core.deltas.path.Path
import deltas.bytecode.coreInstructions.integers.AddIntegersDelta
import deltas.bytecode.coreInstructions.longs.AddLongsDelta
import deltas.javac.expressions.{ExpressionInstance, ExpressionSkeleton}
import deltas.bytecode.types.{IntTypeC, LongTypeC, TypeSkeleton}

object AdditionDelta extends DeltaWithGrammar with ExpressionInstance {

  val key = Clazz

  override def toByteCode(addition: Path, compilation: Compilation): Seq[Node] = {
    val toInstructions = ExpressionSkeleton.getToInstructions(compilation)
    val firstInstructions = toInstructions(getFirst(addition))
    val secondInstructions = toInstructions(getSecond(addition))
    firstInstructions ++ secondInstructions ++ (getType(addition, compilation) match {
      case x if x == IntTypeC.intType => Seq(AddIntegersDelta.addIntegers())
      case x if x == LongTypeC.longType => Seq(AddLongsDelta.addLongs())
      case _ => throw new NotImplementedError()
    })
  }

  override def getType(expression: Path, compilation: Compilation): Node = {
    val getType = ExpressionSkeleton.getType(compilation)
    val firstType = getType(getFirst(expression))
    val secondType = getType(getSecond(expression))
    firstType match
    {
      case x if x == IntTypeC.intType =>
        TypeSkeleton.checkAssignableTo(compilation)(IntTypeC.intType, firstType)
        TypeSkeleton.checkAssignableTo(compilation)(IntTypeC.intType, secondType)
        IntTypeC.intType
      case x if x == LongTypeC.longType =>
        TypeSkeleton.checkAssignableTo(compilation)(LongTypeC.longType, firstType)
        TypeSkeleton.checkAssignableTo(compilation)(LongTypeC.longType, secondType)
        LongTypeC.longType
      case _ => throw new NotImplementedError()
    }

  }

  def getFirst[T <: NodeLike](addition: T) = addition(First).asInstanceOf[T]

  def getSecond[T <: NodeLike](addition: T) = addition(Second).asInstanceOf[T]

  override def dependencies: Set[Contract] = Set(AddAdditivePrecedence, AddIntegersDelta)

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit =  {
    import grammars._
    val additiveGrammar = find(AddAdditivePrecedence.Grammar)
    val parseAddition = additiveGrammar.as(First) ~~< "+" ~~ additiveGrammar.as(Second) asNode Clazz
    additiveGrammar.addOption(parseAddition)
  }

  def addition(first: Node, second: Node) = new Node(Clazz, First -> first, Second -> second)

  object Clazz extends NodeClass

  object First extends NodeField

  object Second extends NodeField

  override def description: String = "Adds the + operator."
}