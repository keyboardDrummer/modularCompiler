package transformations.types

import core.exceptions.BadInputException
import core.particles._
import core.particles.grammars.GrammarCatalogue
import core.particles.node.Node
import transformations.bytecode.ByteCodeSkeleton

class TypeMismatchException(to: Node, from: Node) extends BadInputException {
  override def toString = s"cannot assign: $to = $from"
}

class NoCommonSuperTypeException(first: Node, second: Node) extends BadInputException

class AmbiguousCommonSuperTypeException(first: Node, second: Node) extends BadInputException

object TypeSkeleton extends ParticleWithGrammar with WithState {

  def toStackType(_type: Node, state: CompilationState) : Node = {
    getState(state).instances(_type.clazz).getStackType(_type, state)
  }

  def getTypeSize(_type: Node, state: CompilationState): Int = getState(state).stackSize(_type.clazz)

  def getByteCodeString(state: CompilationState): Node => String =
    _type => getState(state).toByteCodeString(_type.clazz)(_type)

  override def dependencies: Set[Contract] = Set(ByteCodeSkeleton)

  def checkAssignableTo(state: CompilationState)(to: Node, from: Node) = {
    if (!isAssignableTo(state)(to, from))
      throw new TypeMismatchException(to, from)
  }

  def isAssignableTo(state: CompilationState)(to: Node, from: Node): Boolean = {
    val fromSuperTypes = getSuperTypes(state)(from)
    if (to.equals(from))
      return true

    fromSuperTypes.exists(_type => _type.equals(to))
  }

  def getSuperTypes(state: CompilationState)(_type: Node) = getSuperTypesRegistry(state)(_type.clazz)(_type)

  def getSuperTypesRegistry(state: CompilationState) = {
    getState(state).superTypes
  }

  def union(state: CompilationState)(first: Node, second: Node): Node = {
    val filteredDepths = getAllSuperTypes(state)(first).map(depthTypes => depthTypes.filter(_type => isAssignableTo(state)(_type, second)))
    val resultDepth = filteredDepths.find(depth => depth.nonEmpty).getOrElse(throw new NoCommonSuperTypeException(first, second))
    if (resultDepth.size > 1)
      throw new AmbiguousCommonSuperTypeException(first, second)

    resultDepth.head
  }

  def getAllSuperTypes(state: CompilationState)(_type: Node): Stream[Set[Node]] = {
    var returnedTypes = Set.empty[Node]
    Stream.iterate(Set(_type))(previousDepthTypes => {
      val result = previousDepthTypes.flatMap(_type => getSuperTypes(state)(_type)).filter(_type => !returnedTypes.contains(_type))
      returnedTypes ++= result
      result
    })
  }

  override def transformGrammars(grammars: GrammarCatalogue): Unit = {
    grammars.create(TypeGrammar)
  }

  def createState = new State
  
  class State {
    val superTypes = new ClassRegistry[Node => Seq[Node]]()
    val toByteCodeString = new ClassRegistry[Node => String]()
    val stackSize = new ClassRegistry[Int]()
    val instances = new ClassRegistry[TypeInstance]
  }

  object TypeGrammar

  override def description: String = "Defines the concept of a type."
}