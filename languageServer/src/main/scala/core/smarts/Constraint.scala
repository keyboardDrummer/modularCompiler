package core.smarts

import core.smarts.objects.{Declaration, DeclarationVariable}
import core.smarts.scopes.objects.{Scope, ScopeVariable}
import core.smarts.types.objects.{Type, TypeVariable}
import langserver.types.Diagnostic

trait Constraint {
  def apply(solver: ConstraintSolver): Boolean

  def instantiateDeclaration(variable: DeclarationVariable, instance: Declaration): Unit = {}
  def instantiateType(variable: TypeVariable, instance: Type): Unit = {}
  def instantiateScope(variable: ScopeVariable, instance: Scope): Unit = {}
  def boundTypes: Set[Type] = Set.empty

  def getDiagnostic: Option[Diagnostic] = None
}
