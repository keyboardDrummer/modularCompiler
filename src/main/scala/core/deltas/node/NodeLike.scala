package core.deltas.node

import scala.collection.mutable

object NodeWrapper
{
  implicit def wrapList[TNodeWrapper, T <: NodeLike](list: Seq[T])(implicit wrap: T => TNodeWrapper): Seq[TNodeWrapper] =
    list.map(n => wrap(n))
  implicit def unwrapList[T <: NodeLike](list: Seq[NodeWrapper[T]]): Seq[T] = list.map(n => n.node)
  implicit def unwrap[T <: NodeLike] (wrapper: NodeWrapper[T]): T = wrapper.node
}

trait NodeWrapper[T <: NodeLike] {
  def node: T

  def get(key: NodeField): Option[Any] = node.get(key)
  def apply(key: NodeField): Any = node.apply(key)
  def update(key: NodeField, value: Any): Unit = node.update(key, value)
  def clazz = node.clazz
  def clazz_=(value: NodeClass) = node.clazz = value
  def dataView: Map[NodeField, Any] = node.dataView
}

trait NodeLike {
  type Self <: NodeLike
  def get(key: NodeField): Option[Any]
  def apply(key: NodeField): Any
  def update(key: NodeField, value: Any): Unit
  def clazz: NodeClass
  def clazz_=(value: NodeClass): Unit
  def dataView: Map[NodeField, Any]

  def selfAndDescendants: List[Self] = {
    var result = List.empty[Self]
    visit(node => result = node :: result)
    result
  }

  def visit(afterChildren: (Self) => Unit = _ => {},
            beforeChildren: (Self) => Boolean = _ => true,
            visited: mutable.Set[Self] = new mutable.HashSet[Self]()): Unit = {

    transformNode(this.asInstanceOf[Self])
    def transformNode(node: Self): Unit = {
      if (!visited.add(node))
        return

      if (!beforeChildren(node))
        return

      val children = node.dataView.values
      for(child <- children)
      {
        child match {
          case metaObject: NodeLike =>
            transformNode(metaObject.asInstanceOf[Self])
          case sequence: Seq[_] =>
            sequence.reverse.foreach({ //TODO: the reverse is a nasty hack to decrease the chance of mutations conflicting with this iteration. Problem would occur when transforming two consecutive declarationWithInitializer's
              case metaChild: NodeLike => transformNode(metaChild.asInstanceOf[Self])
              case _ =>
            })
          case _ =>
        }
      }

      afterChildren(node)
    }
  }
}