package core.language

import languageServer.{FilePosition, FileRange, SourceRange}

trait FileSourceElement {
  def range: SourceRange

  def childElements: Seq[FileSourceElement]
}

case class SourceElementFromFileElement(uri: String, element: FileSourceElement) extends SourceElement {
  override def range = Some(element.range)

  override def uriOption = Some(uri)

  override def childElements: Seq[SourceElementFromFileElement] = {
    element.childElements.map(e => SourceElementFromFileElement(uri, e))
  }
}

trait SourceElement {
  /*
  A None value means the element is not part of the source.
   */
  def range: Option[SourceRange]

  def childElements: Seq[SourceElement] = Seq.empty

  /*
  A None value means the element is not part of the source.
   */
  def fileRange: Option[FileRange] = range.flatMap(p => uriOption.map(u => FileRange(u, p)))

  def uriOption: Option[String]

  def isInAnotherFile(uri: String): Boolean = {
    uriOption match {
      case Some(nodeUri) => uri != nodeUri
      case None => false
    }
  }

  def getChildForPosition(filePosition: FilePosition): Option[SourceElement] = {
    if (isInAnotherFile(filePosition.uri))
      return None

    if (!range.exists(r => r.contains(filePosition.position)))
      return None

    val childResults = childElements.flatMap(child => child.getChildForPosition(filePosition))
    Some(childResults.headOption.getOrElse({
      this
    }))
  }
}
