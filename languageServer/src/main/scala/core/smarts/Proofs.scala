package core.smarts

import core.language.SourceElement
import core.smarts.objects.{Declaration, DeclarationVariable, NamedDeclaration, Reference}
import core.smarts.scopes.ScopeGraph
import core.smarts.types.TypeGraph
import core.smarts.types.objects.{Type, TypeFromDeclaration, TypeVariable}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Proofs {

  var mappedTypeVariables: Map[TypeVariable, Type] = Map.empty
  var mappedDeclarationVariables: Map[DeclarationVariable, Declaration] = Map.empty
  val scopeGraph = new ScopeGraph
  val typeGraph = new TypeGraph
  var environment = Map.empty[Declaration, Type]
  var references = Map.empty[Reference, NamedDeclaration]
  var referencesPerDeclaration = mutable.Map.empty[NamedDeclaration, ArrayBuffer[Reference]]

  def resolveDeclaration(declaration: Declaration): Declaration = declaration match {
    case v: DeclarationVariable => mappedDeclarationVariables.get(v) match
    {
      case Some(value) => resolveDeclaration(value)
      case _ => declaration
    }
    case _ => declaration
  }

  def resolveType(_type: Type): Type = _type match {
    case TypeFromDeclaration(declaration) => TypeFromDeclaration(resolveDeclaration(declaration))
    case v: TypeVariable => mappedTypeVariables.get(v) match
    {
      case Some(value) => resolveType(value)
      case _ => _type
    }
    case _ => _type
  }

  def gotoDefinition(location: SourceElement): Option[NamedDeclaration] = {
    val maybeReference = scopeGraph.getReferenceFromSourceElement(location)
    maybeReference.flatMap(reference => references.get(reference))
  }

  def findReferences(location: SourceElement): Seq[Reference] = {
    val maybeDeclaration = scopeGraph.findDeclaration(location)
    maybeDeclaration.toSeq.flatMap(declaration => findReferences(declaration))
  }

  def findReferences(declaration: NamedDeclaration): Seq[Reference] = {
    referencesPerDeclaration.getOrElse(declaration, ArrayBuffer.empty)
  }

  def getDeclarationsInScope(location: SourceElement): Seq[NamedDeclaration] = {
    val maybeReference = scopeGraph.getReferenceFromSourceElement(location)
    val declarations = maybeReference.map(reference => scopeGraph.resolveWithoutNameCheck(reference)).getOrElse(Seq.empty)
    declarations.filter(declaration => declaration.origin.nonEmpty)
  }

  def addResolution(reference: Reference, declaration: NamedDeclaration): Unit = {
    references += reference -> declaration
    val declarationReferences = referencesPerDeclaration.getOrElseUpdate(declaration, ArrayBuffer.empty)
    declarationReferences.append(reference)
  }
}