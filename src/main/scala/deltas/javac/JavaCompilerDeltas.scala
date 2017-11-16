package deltas.javac

import application.compilerCockpit.PrettyPrint
import core.deltas._
import deltas.bytecode._
import deltas.bytecode.additions.{LabelledLocations, PoptimizeC}
import deltas.bytecode.attributes._
import deltas.bytecode.constants._
import deltas.bytecode.coreInstructions._
import deltas.bytecode.coreInstructions.doubles.DoubleReturnInstructionDelta
import deltas.bytecode.coreInstructions.floats.FloatReturnInstructionDelta
import deltas.bytecode.coreInstructions.integers._
import deltas.bytecode.coreInstructions.integers.integerCompare._
import deltas.bytecode.coreInstructions.longs._
import deltas.bytecode.coreInstructions.objects._
import deltas.bytecode.extraBooleanInstructions._
import deltas.bytecode.extraConstants.{QualifiedClassNameConstantDelta, TypeConstant}
import deltas.bytecode.simpleBytecode.{InferredMaxStack, InferredStackFrames, RemoveConstantPool}
import deltas.bytecode.types._
import deltas.javaPlus.ExpressionMethodDelta
import deltas.javac.classes._
import deltas.javac.classes.skeleton.JavaClassSkeleton
import deltas.javac.constructor._
import deltas.javac.expressions._
import deltas.javac.expressions.additive.{AddAdditivePrecedence, AdditionDelta, SubtractionC}
import deltas.javac.expressions.equality.{AddEqualityPrecedence, EqualityDelta}
import deltas.javac.expressions.literals.{BooleanLiteralC, IntLiteralDelta, LongLiteralC, NullC}
import deltas.javac.expressions.postfix.PostFixIncrementC
import deltas.javac.expressions.prefix.NotC
import deltas.javac.expressions.relational.{AddRelationalPrecedence, GreaterThanC, LessThanC}
import deltas.javac.methods._
import deltas.javac.methods.assignment.{AssignToVariable, AssignmentPrecedence, AssignmentSkeleton, IncrementAssignmentC}
import deltas.javac.methods.call.CallStaticOrInstanceC
import deltas.javac.statements._
import deltas.javac.statements.locals.{LocalDeclarationC, LocalDeclarationWithInitializerC}
import deltas.javac.trivia.{CaptureTriviaDelta, JavaStyleCommentsDelta, TriviaInsideNode}
import deltas.javac.types._

object JavaCompilerDeltas {

  def getCompiler = new CompilerFromDeltas(javaCompilerDeltas)

  def allDeltas = javaCompilerDeltas ++
    Seq(JavaStyleCommentsDelta, CaptureTriviaDelta, TriviaInsideNode, ExpressionMethodDelta, BlockCompilerDelta, JavaGotoC)

  def javaCompilerDeltas: Seq[Delta] = {
    Seq(ClassifyTypeIdentifiers, DefaultConstructorDelta, ImplicitSuperConstructorCall, ImplicitObjectSuperClass,
      NewC, FieldDeclarationWithInitializer, ConstructorDelta, SelectorReferenceKind, VariableReferenceKind) ++
      Seq(ThisCallExpression, SuperCallExpression, ThisVariable) ++ fields ++ imports ++
      javaMethod
  }

  def imports = Seq(ImplicitJavaLangImport, WildcardImportC, BasicImportC)
  def fields = Seq(FieldDeclaration, AssignToMember)

  def javaMethod = Seq(ForLoopContinueC, JavaGotoC, ForLoopC, LocalDeclarationWithInitializerC) ++
    Seq(ImplicitReturnAtEndOfMethod, ImplicitThisForPrivateMemberSelection, ReturnExpressionDelta, ReturnVoidDelta, CallStaticOrInstanceC, SelectField, MemberSelector) ++ methodBlock
  def methodBlock = Seq(LocalDeclarationC, IncrementAssignmentC, AssignToVariable, AssignmentSkeleton,
    AssignmentPrecedence, PostFixIncrementC, VariableC) ++ Seq(MethodDelta, AccessibilityFieldsDelta) ++ Seq(JavaClassSkeleton) ++ javaSimpleStatement

  def javaSimpleStatement = Seq(IfThenElseC, IfThenC, WhileContinueC, WhileC, BlockDelta,
    ExpressionAsStatementC, StatementSkeleton) ++ javaSimpleExpression

  def javaSimpleExpression: Seq[Delta] = Seq(TernaryC, EqualityDelta,
    AddEqualityPrecedence, LessThanC, GreaterThanC, AddRelationalPrecedence, AdditionDelta, SubtractionC, AddAdditivePrecedence,
    BooleanLiteralC, LongLiteralC, IntLiteralDelta, NullC, NotC, ParenthesisC, ExpressionSkeleton) ++ allByteCodeTransformations

  def allByteCodeTransformations = Seq(OptimizeComparisonInstructionsC) ++
    Seq(LessThanInstructionC, GreaterThanInstructionC, NotInstructionC, IntegerEqualsInstructionC, ExpandVirtualInstructionsC) ++
    simpleByteCodeTransformations

  def simpleByteCodeTransformations: Seq[Delta] = Seq(PoptimizeC) ++
    Seq(InferredStackFrames, InferredMaxStack, LabelledLocations, RemoveConstantPool) ++ byteCodeTransformations

  def byteCodeTransformations = byteCodeInstructions ++ byteCodeWithoutInstructions

  def byteCodeInstructions: Seq[InstructionDelta] = {
    Seq(Pop2Delta, PopDelta, GetStaticDelta, GotoDelta, IfIntegerCompareLessDelta, IfIntegerCompareLessOrEqualDelta,
      IfZeroDelta, IfNotZero, InvokeSpecialDelta, InvokeVirtualDelta, InvokeStaticDelta, NewByteCodeDelta, Duplicate2InstructionDelta, DuplicateInstructionDelta) ++
      objectInstructions ++ Seq(PushNullDelta, StoreIntegerDelta, SubtractIntegerDelta, VoidReturnInstructionDelta,
      SwapInstruction, GetFieldDelta, PutField) ++
      integerInstructions ++ longInstructions ++ floatInstructions ++ doubleInstructions
  }

  def objectInstructions = Seq(LoadAddressDelta, AddressReturnInstructionDelta, StoreAddressDelta)

  def doubleInstructions = Seq(DoubleReturnInstructionDelta)

  def floatInstructions = Seq(FloatReturnInstructionDelta)

  def longInstructions = Seq(LongReturnInstructionDelta, AddLongsDelta, CompareLongDelta, PushLongDelta, LoadLongDelta, StoreLongDelta)

  def integerInstructions = Seq(AddIntegersDelta, SmallIntegerConstantDelta, LoadConstantDelta, IncrementIntegerDelta, IntegerReturnInstructionDelta, LoadIntegerDelta, IfIntegerCompareGreaterOrEqualDelta,
    IfIntegerCompareEqualDelta, IfIntegerCompareNotEqualDelta)

  def byteCodeWithoutInstructions = byteCodeWithoutTextualParser ++ Seq(ParseUsingTextualGrammar)

  def byteCodeWithoutTextualParser: Seq[DeltaWithGrammar] = bytecodeAttributes ++ constantEntryParticles ++
    Seq(ByteCodeMethodInfo, ByteCodeFieldInfo) ++
    typeTransformations ++ Seq(ByteCodeSkeleton)

  val bytecodeAttributes: Seq[DeltaWithGrammar] = Seq(StackMapTableAttribute, LineNumberTable, SourceFileAttribute,
    CodeAttribute, //ExceptionsAttribute, InnerClassesAttribute,
    SignatureAttribute)

  def constantEntryParticles = Seq(QualifiedClassNameConstantDelta, TypeConstant) ++ Seq(MethodTypeConstant, Utf8ConstantDelta, DoubleInfoConstant, LongInfoConstant, FieldRefConstant, InterfaceMethodRefConstant, MethodRefConstant, NameAndTypeConstant,
    ClassInfoConstant, IntegerInfoConstant, StringConstant, MethodHandleConstant, MethodType,
    InvokeDynamicConstant)

  def typeTransformations = Seq(SelectInnerClassC, TypeVariable, TypeAbstraction, WildcardTypeArgument, ExtendsTypeArgument,
    SuperTypeArgument, TypeApplication, MethodType) ++
    Seq(ObjectTypeDelta, ArrayTypeC, ByteTypeC, FloatTypeC, CharTypeC, BooleanTypeC, DoubleTypeC, LongTypeC, VoidTypeC, IntTypeC,
      ShortTypeC, TypeSkeleton)

  def spliceBeforeTransformations(implicits: Seq[Delta], splice: Seq[Delta]): Seq[Delta] =
    getCompiler.spliceBeforeTransformations(implicits, splice)

  def spliceAfterTransformations(implicits: Seq[Delta], splice: Seq[Delta]): Seq[Delta] =
    getCompiler.spliceAfterTransformations(implicits, splice)

  def getPrettyPrintJavaToByteCodeCompiler = {
    new CompilerFromDeltas(spliceBeforeTransformations(JavaCompilerDeltas.byteCodeTransformations, Seq(new PrettyPrint)))
  }
}


