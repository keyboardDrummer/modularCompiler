
package deltas.yaml

import core.SourceUtils
import core.bigrammar.SelectGrammar
import core.deltas.path.{ChildPath, PathRoot}
import core.language.Compilation
import core.parsers.editorParsers.UntilBestAndXStepsStopFunction
import deltas.expression.{ArrayLiteralDelta, ExpressionDelta}
import deltas.json.{JsonObjectLiteralDelta, JsonStringLiteralDelta}
import org.scalatest.funsuite.AnyFunSuite
import util.TestLanguageBuilder

class YamlTest extends AnyFunSuite {

  val language = TestLanguageBuilder.buildWithParser(YamlLanguage.deltasWithoutParser,
    stopFunction = UntilBestAndXStepsStopFunction(), indentationSensitive = true)

  test("plain scalar with -") {
    val program = """AllowedPattern: ([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)"""
    val compilation = language.compileString(program)
    assert(compilation.diagnostics.isEmpty)
  }

  test("regression") {
    val program =
      """- Bar:
        |   Joo
        |- 2""".stripMargin
    val compilation = language.compileString(program)
    assert(compilation.diagnostics.isEmpty)
  }

  test("regression 2") {
    val program = "- Bar:\n   \n- 2"
    val compilation = language.compileString(program)
    // TODO fix plain scalar so these asserts can be swapped.
    assert(compilation.diagnostics.isEmpty)
    // assert(compilation.diagnostics.size == 1 && compilation.diagnostics.head.diagnostic.message.contains("expected '<value>'"))
  }

  test("error case") {
    val program = """Foo: bar
                    | yoo: hee""".stripMargin
    val compilation = language.compileString(program)
    assert(compilation.diagnostics.size == 3 && compilation.diagnostics.head.diagnostic.message.contains("["))
  }

  test("array as key") {
    val program = """Foo: [bar,
                    | yoo]: hee""".stripMargin
    val compilation = language.compileString(program)
    assert(compilation.diagnostics.isEmpty)
  }

  test("compact array") {
    val input = """SecurityGroupIngress:
                  |- IpProtocol: tcp
                  |  FromPort: 22""".stripMargin

    val compilation = language.compileString(input)
    assert(compilation.diagnostics.isEmpty)
    val array = JsonObjectLiteralDelta.ObjectLiteral(compilation.program.asInstanceOf[PathRoot].current).members.head.value
    val arrayMembers = ArrayLiteralDelta.ArrayLiteral(array).members
    val nestedObject = JsonObjectLiteralDelta.ObjectLiteral(arrayMembers.head)
    assert(nestedObject.members.length == 2)
  }

  test("compact array with negative indentation") {
    val input = """ SecurityGroupIngress:
                  |- IpProtocol: tcp
                  |  FromPort: 22""".stripMargin

    val compilation = language.compileString(input)
    assert(compilation.diagnostics.nonEmpty)
  }

  test("single member object without a value") {
    val program = "Key:"
    val compilation = language.compileString(program)
    replaceDefaultWithDefaultString(compilation)

    val reference = "Key:'default'"
    val referenceCompilation = language.compileString(reference)
    assertResult(referenceCompilation.program)(compilation.program)
    assert(compilation.diagnostics.size == 1)
  }

  val twoMemberObject =
    """Missing: default
      |Key: Value
    """.stripMargin
  lazy val twoMemberObjectCompilation = language.compileString(twoMemberObject)

  test("two member object with no first value") {
    val program =
      """Missing:
        |Key: Value""".stripMargin
    val compilation = language.compileString(program)
    replaceDefaultWithDefaultString(compilation)

    val reference =
      """Missing: default
        |Key: Value
      """.stripMargin
    val referenceCompilation = language.compileString(reference)
    assertResult(referenceCompilation.program)(compilation.program)
    assert(compilation.diagnostics.size == 1)
  }

  test("two member object with no first value and colon") {
    val program =
      """Missing
        |Key: Value
      """.stripMargin
    val compilation = language.compileString(program)
    replaceDefaultWithDefaultString(compilation)

    assertResult(twoMemberObjectCompilation.program)(compilation.program)
    assert(compilation.diagnostics.size == 1)
  }


  val twoObjectsSingleMemberEach =
    """Parent1:
      |  HasValue: Value Value Value
      |  MissingValue: default
      |Parent2:
      |  HasValue2: Value2
    """.stripMargin
  lazy val twoObjectsSingleMemberEachCompilation = language.compileString(twoObjectsSingleMemberEach)

  test("complicated middle errors") {
    val program =
      """Parent1:
        |  HasValue: Value Value Value
        |  MissingValue
        |Parent2:
        |  HasValue2: Value2
      """.stripMargin
    val compilation = language.compileString(program)

    replaceDefaultWithDefaultString(compilation)
    assertResult(twoObjectsSingleMemberEachCompilation.program)(compilation.program)
    assert(compilation.diagnostics.size == 1)
  }

  test("Broken in the middle") {
    val program =
      """Parameters:
        |  KeyName: EC2
        |  MemberWithOnlyKeyAndColon:
        |Resources:
        |  MemberWithOnlyKey
        |  LaunchConfig:
        |    Type: AWS
      """.stripMargin
    val compilation = language.compileString(program)
    assert(compilation.diagnostics.size == 2)
  }

  private def replaceDefaultWithDefaultString(compilation: Compilation): Unit = {
    compilation.program.asInstanceOf[PathRoot].visitShape(ExpressionDelta.DefaultShape,
      p => p.asInstanceOf[ChildPath].replaceWith(JsonStringLiteralDelta.neww("default")))
  }

  test("tagged block key") {
    val input = """      UserData: !Base64
                  |        Fn::Join:
                  |          - ''
                  |          - ['#!/bin/bash -xe
                  |
                  |            ', 'yum update -y aws-cfn-bootstrap
                  |
                  |            ', '/opt/aws/bin/cfn-init -v ', '         --stack ', !Ref 'AWS::StackName',
                  |             '         --resource LaunchConfig ', '         --region ', !Ref 'AWS::Region',
                  |             '
                  |
                  |            ', '/opt/aws/bin/cfn-signal -e $? ', '         --stack ', !Ref 'AWS::StackName',
                  |             '         --resource WebServerGroup ', '         --region ', !Ref 'AWS::Region',
                  |             '
                  |
                  |            ']
                  |""".stripMargin

    val compilation = language.compileString(input)
    assert(compilation.diagnostics.isEmpty)
  }

  test("plain scalar") {
    val contents =
      """Blaa
        |Comment
      """.stripMargin

    val language = TestLanguageBuilder.buildWithParser(Seq(new SelectGrammar(ExpressionDelta.FirstPrecedenceGrammar),
      PlainScalarDelta, ExpressionDelta))
    val compilation = language.compileString(contents)
    assert(compilation.diagnostics.nonEmpty)
  }

  val deltas = Seq(new SelectGrammar(YamlCoreDelta.BlockValue),
    YamlObjectDelta, YamlArrayDelta, YamlCoreDelta, ArrayLiteralDelta, PlainScalarDelta, ExpressionDelta)
  val blockLanguage = TestLanguageBuilder.buildWithParser(deltas,
    stopFunction = UntilBestAndXStepsStopFunction(), indentationSensitive = true)

  test("plain scalar 2") {
    val contents =
      """Metadata:
        |  Blaa
        |  Comment""".stripMargin

    val compilation = blockLanguage.compileString(contents)
    assert(compilation.diagnostics.nonEmpty)
  }

  test("composite") {
    val contents =
      """Metadata:
        |  Comment: Install a simple application
        |  Blaa
        |  AWS: Bar""".stripMargin

    val compilation = blockLanguage.compileString(contents)
    assert(compilation.diagnostics.size == 1)
    assert(compilation.diagnostics.head.diagnostic.message.contains(":<value>"))
  }

  // TODO if this test parses too many steps it gets a stack overflow, fix.
  test("big yaml file") {
    val contents = SourceUtils.getResourceFileContents("AutoScalingMultiAZWithNotifications.yaml")

    val compilation = language.compileString(contents)
    assert(compilation.diagnostics.isEmpty)
  }
}
