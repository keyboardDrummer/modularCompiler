package transformations.bytecode

import core.transformation.Contract

object SimpleByteCode extends Contract {
  override def dependencies: Set[Contract] = Set(InferredStackFrames, InferredMaxStack)
}
