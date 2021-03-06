package miksilo.modularLanguages.deltas.javac.methods

import miksilo.languageServer.core.language.Language
import miksilo.languageServer.core.language.exceptions.BadInputException
import miksilo.modularLanguages.core.node.Node
import miksilo.modularLanguages.deltas.bytecode.types.TypeSkeleton

case class VariableDoesNotExist(name: String) extends BadInputException {
  override def toString = s"variable '$name' does not exist."
}

case class VariableInfo(offset: Integer, _type: Node)

case class VariablePool(language: Language, typedVariables: Map[String, Node] = Map.empty) {
  private var variables = Map.empty[String, VariableInfo]
  var offset = 0
  for(typedVariable <- typedVariables)
    privateAdd(typedVariable._1, typedVariable._2)

  def localCount: Int = offset

  def get(name: String): Option[VariableInfo] = variables.get(name)

  def apply(name: String): VariableInfo = variables.getOrElse(name, throw VariableDoesNotExist(name))

  def contains(name: String): Boolean = variables.contains(name)

  private def privateAdd(variable: String, _type: Node): Unit = {
    variables = variables.updated(variable, VariableInfo(offset, _type))
    offset += TypeSkeleton.getTypeSize(_type, language)
  }

  def add(variable: String, _type: Node): VariablePool = {
    VariablePool(language, typedVariables.updated(variable, _type))
  }
}
