package deltas.javac

import core.deltas._
import core.deltas.grammars.LanguageGrammars
import core.deltas.node.Node
import core.deltas.path._
import core.language.Language
import deltas.bytecode.ByteCodeSkeleton
import deltas.javac.classes.ClassCompiler
import deltas.javac.classes.skeleton.JavaClassSkeleton.getState
import deltas.javac.classes.skeleton.{ClassMember, ClassSignature, JavaClassSkeleton}
import deltas.javac.expressions.ExpressionSkeleton
import deltas.javac.methods.call.CallDelta
import deltas.javac.methods.{MemberSelector, MethodDelta, VariableDelta}

object ImplicitThisForPrivateMemberSelection extends DeltaWithPhase with DeltaWithGrammar {
  val thisName: String = "this"

  override def dependencies: Set[Contract] = Set(MethodDelta, JavaClassSkeleton)

  def addThisToVariable(compilation: Compilation, variable: NodePath) {
    val compiler = JavaClassSkeleton.getClassCompiler(compilation)

    val name = VariableDelta.getVariableName(variable)
    val variableWithCorrectPath: NodePath = getVariableWithCorrectPath(variable)
    val scopes = MethodDelta.getMethodCompiler(compilation).bindingsAndTypes.scopes
    val reference = scopes.findReference(variable)
    //scopes.resolve(reference).
    ???
//    if (!MethodDelta.getMethodCompiler(compilation).bindingsAndTypes.scopes.findReference(variable)variableWithCorrectPath).contains(name)) {
//      val currentClass = compiler.currentClassInfo
//      currentClass.methods.keys.find(key => key.methodName == name).foreach(key => {
//        val classMember: ClassMember = currentClass.methods(key)
//        addThisToVariable(classMember, currentClass, variable)
//      })
//
//      currentClass.fields.keys.find(key => key == name).foreach(key => {
//        val classMember = currentClass.fields(key)
//        addThisToVariable(classMember, currentClass, variable)
//      })
//    }
  }

  def addThisToVariable(classMember: ClassMember, currentClass: ClassSignature, variable: NodePath): Unit = {
    val name = VariableDelta.getVariableName(variable)
    val newVariableName = if (classMember._static) currentClass.name else thisName
    val selector = MemberSelector.selector(VariableDelta.variable(newVariableName), name)
    variable.replaceWith(selector)
  }

  def getVariableWithCorrectPath(obj: NodePath): NodePath = {
    if (obj.shape == MethodDelta.Shape)
      return NodePathRoot(obj.current)

    obj match {
      case NodeFieldValue(parent, field) => new NodeFieldValue(getVariableWithCorrectPath(parent), field)
      case NodeSequenceElement(parent, field, index) => new NodeSequenceElement(getVariableWithCorrectPath(parent), field, index)
    }
  }

  override def description: String = "Implicitly prefixes references to private methods with the 'this' qualified if it is missing."

  override def transformProgram(program: Node, compilation: Compilation): Unit = {
    val programWithOrigin = NodePathRoot(program)
    programWithOrigin.visit(beforeChildren = obj => { obj.shape match {
        case ByteCodeSkeleton.Shape =>
          JavaLang.loadIntoClassPath(compilation)

          val classCompiler = ClassCompiler(obj, compilation)
          getState(compilation).classCompiler = classCompiler
          classCompiler.bind()

        case MethodDelta.Shape => MethodDelta.setMethodCompiler(obj, compilation)
        case VariableDelta.VariableKey => addThisToVariable(compilation, obj)
        case _ =>
      }
      true
    })
  }

  override def transformGrammars(grammars: LanguageGrammars, state: Language): Unit = {
    val callee = grammars.find(CallDelta.CallCallee)
    val expression = grammars.find(ExpressionSkeleton.ExpressionGrammar)
    callee.inner = expression
  }
}
