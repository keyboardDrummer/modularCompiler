package miksilo.languageServer.core.smarts

import miksilo.languageServer.core.smarts.objects.{Declaration, DeclarationVariable, NamedDeclaration, Reference}
import miksilo.languageServer.core.smarts.scopes.imports.DeclarationOfScope
import miksilo.languageServer.core.smarts.scopes.objects.{ConcreteScope, _}
import miksilo.languageServer.core.smarts.scopes.{DeclarationInsideScope, ParentScope, ReferenceInScope}
import miksilo.languageServer.core.smarts.types.objects.{Type, TypeFromDeclaration, TypeVariable}
import miksilo.languageServer.core.smarts.types._
import miksilo.languageServer.server.SourcePath

import scala.collection.mutable

case class Copy(key: AnyRef, counter: Int)
class ConstraintBuilder(val factory: Factory) {

  var proofs: Proofs = _

  val typeVariables: scala.collection.mutable.Map[String, TypeVariable] = new mutable.HashMap   //TODO deze moeten nog resetten

  def scopeVariable(parent: Option[Scope] = None): ScopeVariable = {
    val result = factory.scopeVariable()
    parent.foreach(p => constraints ::= ParentScope(result, p))
    result
  }

  def typeVariable(origin: Option[SourcePath] = None): TypeVariable = factory.typeVariable(origin)

  var constraints: List[Constraint] = List.empty

  def newScope(parent: Scope = null, debugName: String = "") : ConcreteScope = {
    val result = factory.newScope(debugName)
    Option(parent).foreach(p => add(ParentScope(result, p)))
    result
  }

  def importScope(into: Scope, source: Scope): Unit = add(ParentScope(into, source))

  def resolveToType(name: String, origin: SourcePath, scope: Scope, _type: Type) : DeclarationVariable = {
    val declaration = declarationVariable()
    val reference = new Reference(name, Option(origin))
    add(ReferenceInScope(reference, scope))
    add(new ResolvesToType(reference, declaration, _type))
    declaration
  }

  def resolve(name: String, scope: Scope, origin: SourcePath, _type: Option[Type] = None) = {
    resolveOption(name, Some(origin), scope, _type)
  }

  def resolveOption(name: String, origin: Option[SourcePath], scope: Scope, _type: Option[Type] = None): DeclarationVariable = {
    val declaration = _type.fold(declarationVariable())(t => declarationVariable(t))
    val reference = refer(name, scope, origin)
    constraints ::= ResolvesTo(reference, declaration)
    declaration
  }

  def refer(hasName: String, scope: Scope, origin: Option[SourcePath]): Reference = {
    val result = new Reference(hasName, origin)
    constraints ::= ReferenceInScope(result, scope)
    result
  }

  def declare(name: String, container: Scope, origin: SourcePath = null, _type: Option[Type] = None): NamedDeclaration = { //TODO the order here is inconsistent with resolve.
    val result = new NamedDeclaration(name, Option(origin))
    constraints ::= DeclarationInsideScope(result, container)
    _type.foreach(t => constraints ::= DeclarationHasType(result, t))
    result
  }

  def getCommonSuperType(first: Type, second: Type): Type = { //TODO this doesn't actually work, because it won't find the superType. We need a separate constraint.
    val superType = typeVariable()
    typesAreEqual(first, superType)
    isFirstSubsetOfSecond(second, superType)
    superType
  }

  def specialization(first: Type, second: Type, debugInfo: Any = null): Unit = add(Specialization(first, second, debugInfo))
  def typesAreEqual(first: Type, second: Type): Unit = add(TypesAreEqual(first, second))

  def add(addition: Constraint): Unit = constraints ::= addition
  def add(addition: List[Constraint]): Unit = constraints = addition ++ constraints

  def assignSubType(superType: Type, subType: Type): Unit = add(AssignSubType(subType, superType))

  def declarationVariable(): DeclarationVariable = {
    factory.declarationVariable()
  }

  def getDeclarationOfType(_type: Type): Declaration = {
    val result = declarationVariable()
    add(TypesAreEqual(TypeFromDeclaration(result), _type))
    result
  }

  def getType(declaration: Declaration) : Type = {
    val result = typeVariable()
    add(DeclarationHasType(declaration, result))
    result
  }

  def declarationVariable(_type: Type): DeclarationVariable = {
    val result = factory.declarationVariable()
    constraints ::= DeclarationHasType(result, _type)
    result
  }

  /*
  Get the scope declared by the given declaration
   */
  def getDeclaredScope(declaration: Declaration, scopeName: Any = null): ScopeVariable = {
    val result = scopeVariable(None)
    constraints ::= DeclarationOfScope(declaration, result)
    result
  }

  def declareScope(declaration: Declaration, parent: Scope = null, debugName: String = ""): ConcreteScope = {
    val result = newScope(parent, debugName)
    constraints ::= DeclarationOfScope(declaration, result)
    result
  }

  def getConstraints: Seq[Constraint] = {
    val result = constraints.reverse
    constraints = List.empty
    result
  }

  def isFirstSubsetOfSecond(subType: Type, superType: Type): Unit = {
    add(CheckSubType(subType, superType))
  }

  def toSolver: ConstraintSolver = {
    new ConstraintSolver(this, getConstraints, proofs = if (proofs != null) proofs else new Proofs())
  }
}
