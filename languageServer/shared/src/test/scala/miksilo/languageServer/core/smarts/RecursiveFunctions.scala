package miksilo.languageServer.core.smarts

import miksilo.languageServer.core.smarts.language.expressions._
import miksilo.languageServer.core.smarts.language.types.IntType
import org.scalatest.funsuite.AnyFunSuite

class RecursiveFunctions extends AnyFunSuite with LanguageWriter {

  test("recursive function")
  {
    val incrementInfinitely = Let("f", Lambda("x", Add(1, "f" $ "x")), "f" $ 0)
    Checker.checkExpression(incrementInfinitely)
  }

  test("recursive function typed let")
  {
    val incrementInfinitely = Let("f", Lambda("x", Add(1, "f" $ "x")), "f" $ 0, bindingLanguageType = Some(IntType ==> IntType))
    Checker.checkExpression(incrementInfinitely)
  }

  test("recursive function typed lambda")
  {
    val incrementInfinitely = Let("f", Lambda("x", Add(1, "f" $ "x"), parameterDefinedType = Some(IntType)), "f" $ 0)
    Checker.checkExpression(incrementInfinitely)
  }
}
