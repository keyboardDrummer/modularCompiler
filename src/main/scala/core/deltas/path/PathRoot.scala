package core.deltas.path

import core.deltas.node.{Key, Node}

case class PathRoot(current: Node) extends Path with Key {

  override def parentOption: Option[Path] = None

  override def hashCode(): Int = 1 //TODO obj.hashCode

  override def equals(obj: Any): Boolean = obj.isInstanceOf[PathRoot] //TODO && obj.equals..
  override def pathAsString: String = "Root"
}






