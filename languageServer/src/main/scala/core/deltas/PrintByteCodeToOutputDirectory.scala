package core.deltas

import core.language.Compilation
import deltas.bytecode.PrintByteCode

import scala.reflect.io.File

object PrintByteCodeToOutputDirectory {

  def perform(outputFile: File, compilation: Compilation): Unit = {
    val bytes = PrintByteCode.getBytes(compilation, compilation.program).toArray
    outputFile.createFile()
    val writer = outputFile.outputStream(append = false)
    writer.write(bytes)
    writer.close()
  }
}