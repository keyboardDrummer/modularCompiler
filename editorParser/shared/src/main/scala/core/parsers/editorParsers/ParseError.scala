package core.parsers.editorParsers

import core.parsers.core.{OffsetNode, ParseInput, ParseText}
import core.parsers.editorParsers.Position.PositionOrdering

case class TextEdit(range: SourceRange, newText: String)
case class Fix(title: String, edit: TextEdit)

/**
  * Position in a text document expressed as zero-based line and character offset.
  */
case class Position(line: Int, character: Int)

case class OffsetNodeRange(from: OffsetNode, until: OffsetNode) {
  def toSourceRange(text: ParseText) = SourceRange(from.toPosition(text), until.toPosition(text))
  def toOffsetRange = OffsetRange(from.getAbsoluteOffset(), until.getAbsoluteOffset())
}

case class OffsetRange(from: Int, until: Int) {
  def contains(offset: Int): Boolean = {
    from <= offset && offset <= until
  }
  def toRange(text: ParseText) = SourceRange(text.getPosition(from), text.getPosition(until))
}
case class FileOffsetRange(uri: String, range: OffsetRange)

/**
  * A range in a text document.
  */
case class SourceRange(start: Position, end: Position) {

  def contains(position: Position): Boolean = {
    PositionOrdering.lteq(start, position) && PositionOrdering.lteq(position, end)
  }

  def contains(position: SourceRange): Boolean = {
    PositionOrdering.lteq(start, position.start) && PositionOrdering.lteq(position.end, end)
  }
}

object Position {
  implicit object PositionOrdering extends Ordering[Position] {

    private val ordering = Ordering.by[Position, (Int, Int)](x => (x.line, x.character))
    override def compare(x: Position, y: Position): Int = {
      ordering.compare(x, y)
    }
  }
}

trait ParseError[Input <: ParseInput] {
  //def array: ArrayCharSequence
  def fix: Option[Fix] = None
  def message: String
  def from: Input
  def to: Input
  def text: ParseText
  def range = OffsetNodeRange(from.offsetNode, to.offsetNode).toSourceRange(text)

  def canMerge: Boolean = false
  def penalty: Double
  def score: Double = -penalty * 1
  def append(other: ParseError[Input]): Option[ParseError[Input]] = None

  override def toString = s"$message AT $from"
}
