package core.language.node

case class SourceRange(start: Position, end: Position) {
  def contains(position: Position): Boolean = {
    position == start || position == end || start < position && position < end
  }
}