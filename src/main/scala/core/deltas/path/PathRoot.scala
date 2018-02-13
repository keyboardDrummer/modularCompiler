package core.deltas.path

import core.language.node.{Key, Node, Position, SourceRange}

case class PathRoot(current: Node) extends NodePath with Key {

  override def parentOption: Option[NodePath] = None

  override def hashCode(): Int = 1 //TODO obj.hashCode

  override def equals(obj: Any): Boolean = obj.isInstanceOf[PathRoot] //TODO && obj.equals..
  override def pathAsString: String = "Root"

  override def position: Option[SourceRange] = Some(SourceRange(Position(0, 0), Position(Int.MaxValue, Int.MaxValue)))
}






