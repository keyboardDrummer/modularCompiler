package core.deltas.path

import core.language.node.{Node, NodeField, SourceRange}

class NodeFieldValue(parent: NodePath, field: NodeField) extends FieldValue(parent, field) with ChildPath {

  override def parentOption: Option[NodePath] = Some(parent)

  override def pathAsString: String = s"${parent.pathAsString}/$field"
}

/** A path to a value in an AST. This is similar to a NodePath, except that the value doesn't have to be of type Node.
  */
case class FieldValue(parent: NodePath, field: NodeField) extends AnyChildPath {
  def current: Node = parent.current(field).asInstanceOf[Node]


  override def hashCode(): Int = parent.hashCode() * field.hashCode()

  override def equals(obj: scala.Any): Boolean = obj match {
    case other: FieldValue => other.parent.equals(parent) && other.field.equals(field)
    case _ => false
  }

  override def replaceWith(replacement: Any): Unit = parent(field) = replacement //TODO hier hoort nog .obj. Hoezo compiled dit?


  override def range: Option[SourceRange] = parent.current.sources.get(field).orElse(current.position)
}


