package miksilo.modularLanguages.deltas.javac.expressions

import miksilo.modularLanguages.util.JavaLanguageTest
import scala.reflect.io.Path

class TestIncrementAssignment extends JavaLanguageTest {

  test("incrementAssignment") {
    val inputDirectory = Path("")
    compareWithJavacAfterRunning("IncrementAssignment", inputDirectory)
  }
}
