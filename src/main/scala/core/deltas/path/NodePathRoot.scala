package core.deltas.path

import core.deltas.node.{Key, Node}

case class NodePathRoot(current: Node) extends NodePath with Key {

  override def parentOption: Option[NodePath] = None

  override def hashCode(): Int = 1 //TODO obj.hashCode

  override def equals(obj: Any): Boolean = obj.isInstanceOf[NodePathRoot] //TODO && obj.equals..
  override def pathAsString: String = "Root"
}





