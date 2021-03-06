package miksilo.modularLanguages.deltas.bytecode.readJar

import miksilo.languageServer.core.language.{Compilation, Language}
import miksilo.modularLanguages.core.bigrammar.BiGrammarToParser._
import miksilo.modularLanguages.core.deltas._
import miksilo.modularLanguages.core.deltas.grammars.LanguageGrammars
import miksilo.modularLanguages.core.node.Node
import miksilo.modularLanguages.deltas.bytecode.ByteCodeSkeleton._
import miksilo.modularLanguages.deltas.bytecode.attributes.SignatureAttribute
import miksilo.modularLanguages.deltas.bytecode.constants.ClassInfoConstant
import miksilo.modularLanguages.deltas.bytecode.types.TypeSkeleton.ByteCodeTypeGrammar
import miksilo.modularLanguages.deltas.bytecode.{ByteCodeFieldInfo, ByteCodeMethodInfo, ByteCodeSkeleton}
import miksilo.modularLanguages.deltas.javac.classes.skeleton.{JavaClassDelta, QualifiedClassName}
import miksilo.modularLanguages.deltas.javac.classes.{ConstantPool, FieldDeclarationDelta}
import miksilo.modularLanguages.deltas.javac.methods.AccessibilityFieldsDelta.{Static, Visibility, VisibilityField}
import miksilo.modularLanguages.deltas.javac.methods.{AccessibilityFieldsDelta, MethodParameters}
import miksilo.modularLanguages.deltas.javac.types.{MethodTypeDelta, TypeAbstraction}
import miksilo.modularLanguages.deltas.method.MethodDelta
import miksilo.modularLanguages.deltas.statement.BlockDelta

object DecompileByteCodeSignature extends DeltaWithPhase {

  override def description: String = "Decompiles the field and method signatures in a classfile."

  override def dependencies: Set[Contract] = Set[Contract](SignatureAttribute, ClassInfoConstant)

  val parseTypeProperty = new Property[String => Node](null)

  override def inject(language: Language): Unit = {
    super.inject(language)
    val typeGrammar = LanguageGrammars.grammars.get(language).find(ByteCodeTypeGrammar)
    val parser = toParser(typeGrammar)
    val sourceLessParser = (input: String) => {
      val parseResult = parser.parse(input)
      val result = parseResult.resultOption
      result.foreach(r => r.asInstanceOf[Node].visit(node => {
        node.sources.clear()
      }))
      result
    }
    parseTypeProperty.add(language, input => sourceLessParser(input).get.asInstanceOf[Node])
  }

  override def transformProgram(program: Node, state: Compilation): Unit = {
    val constantPool = program.constantPool
    val classReference = constantPool.getNode(program(ByteCodeSkeleton.ClassNameIndexKey).asInstanceOf[Int])
    val nameIndex = classReference(ClassInfoConstant.Name).asInstanceOf[Int]
    val qualifiedClassName = QualifiedClassName(constantPool.getUtf8(nameIndex).split("/").toSeq)

//    val parentClassReference = constantPool.getNode(program(ByteCodeSkeleton.ClassParentIndex).asInstanceOf[Int])
    //    val nameIndex = classReference(ClassRefConstant.ClassRefName).asInstanceOf[Int]
    //    val qualifiedClassName = new QualifiedClassName(constantPool.getUtf8(nameIndex).split("/").toSeq)

    val fieldInfos = program(ByteCodeSkeleton.ClassFields).asInstanceOf[Seq[Node]]

    val fields = getFields(state, constantPool, fieldInfos)
    val methods = getMethods(state, constantPool, program(ByteCodeSkeleton.Methods).asInstanceOf[Seq[Node]])
    val members = fields ++ methods
    val javaClass = JavaClassDelta.neww(qualifiedClassName.parts.dropRight(1), qualifiedClassName.parts.last, members, List.empty[Node], None)
    program.replaceData(javaClass)

  }

  val accessFlagsToVisibility: Map[ByteCodeMethodInfo.MethodAccessFlag, Visibility] = AccessibilityFieldsDelta.visibilityAccessFlagLinks.
    flatMap(p => p._2.map(flag => (flag, p._1))).toMap

  def getMethods(language: Language, constantPool: ConstantPool, methodInfos: Seq[Node]): Seq[Node] = {
    val parseType = parseTypeProperty.get(language)
    methodInfos.map(methodInfo => {
      val nameIndex: Int = methodInfo(ByteCodeMethodInfo.MethodNameIndex).asInstanceOf[Int]
      val attributes = methodInfo(ByteCodeMethodInfo.MethodAttributes).asInstanceOf[Seq[Node]]
      val signatureAttribute = attributes.find(node => node.shape == SignatureAttribute.shape)
      val _type: Node = signatureAttribute match {
        case Some(signature) =>
          val signatureIndex = signature(SignatureAttribute.SignatureIndex).asInstanceOf[Int]
          val signatureTypeString = constantPool.getUtf8(signatureIndex)
          parseType(signatureTypeString)
        case None =>
          val methodDescriptorIndex = methodInfo(ByteCodeMethodInfo.MethodDescriptor).asInstanceOf[Int]
          val descriptor = constantPool.getUtf8(methodDescriptorIndex)
          val descriptorType: Node = parseType(descriptor)
          descriptorType
      }
      val name: String = constantPool.getUtf8(nameIndex)

      val (methodType, typeParameters) =
        if (_type.shape == TypeAbstraction.TypeAbstractionKey)
          (TypeAbstraction.getBody(_type),TypeAbstraction.getParameters(_type))
        else
          (_type,Seq.empty)
      val returnType = methodType(MethodTypeDelta.ReturnType).asInstanceOf[Node]
      val parameterTypes = methodType(MethodTypeDelta.Parameters).asInstanceOf[Seq[Node]]
	    val parameters = parameterTypes.zipWithIndex.map(parameterTypeWithIndex =>
        MethodParameters.neww("parameter" + parameterTypeWithIndex._2, parameterTypeWithIndex._1))

      val method = MethodDelta.neww(name, returnType, parameters, BlockDelta.neww(), typeParameters = typeParameters)
      setVisibility(methodInfo, method)
      method
    })
  }

  def setVisibility(hasFlags: Node, target: Node): Unit = {
    val accessFlags: Set[ByteCodeMethodInfo.MethodAccessFlag] = Set.empty //TODO fix.
    val foundVisibilities: Set[Visibility] = accessFlags.flatMap(f => accessFlagsToVisibility.get(f))
    val visibility: Visibility = (foundVisibilities ++ Seq(AccessibilityFieldsDelta.DefaultVisibility)).head
    target(VisibilityField) = visibility
    target(Static) = false
  }

  def getFields(language: Language, constantPool: ConstantPool, fieldInfos: Seq[Node]): Seq[Node] = {
    val parseType = parseTypeProperty.get(language)
    fieldInfos.map(fieldInfo => {
      val nameIndex: Int = fieldInfo(ByteCodeFieldInfo.NameIndex).asInstanceOf[Int]
      val attributes = fieldInfo(ByteCodeFieldInfo.FieldAttributes).asInstanceOf[Seq[Node]]
      val signatureAttribute = attributes.find(node => node.shape == SignatureAttribute.shape)
      val _type: Node = signatureAttribute match {
        case Some(signature) =>
          val signatureIndex = signature(SignatureAttribute.SignatureIndex).asInstanceOf[Int]
          val signatureTypeString = constantPool.getUtf8(signatureIndex)
          parseType(signatureTypeString)
        case None =>
          val fieldDescriptorIndex = fieldInfo(ByteCodeFieldInfo.DescriptorIndex).asInstanceOf[Int]
          val descriptor = constantPool.getUtf8(fieldDescriptorIndex)
          val descriptorType: Node = parseType(descriptor)
          descriptorType
      }
      val name: String = constantPool.getUtf8(nameIndex)
      val field = FieldDeclarationDelta.neww(_type, name)
      setVisibility(fieldInfo, field)
      field
    })
  }
}
