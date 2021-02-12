package miksilo.lspprotocol.lsp

import miksilo.editorParser.parsers.core.ParseText
import play.api.libs.json._
import miksilo.editorParser.parsers.editorParsers.{FileOffsetRange, Position, SourceRange, TextEdit}

trait DocumentSymbolProvider {
  def documentSymbols(params: DocumentSymbolParams): Seq[SymbolInformation]
}

trait RenameProvider {
  def rename(params: RenameParams): WorkspaceEdit
}

case class RenameParams(textDocument: TextDocumentIdentifier, position: Position, newName: String)

case class DocumentSymbolParams(textDocument: TextDocumentIdentifier)

trait HoverProvider {
  def hoverRequest(request: TextDocumentHoverRequest): Hover
}

trait CompletionProvider {
  def getOptions: CompletionOptions
  def complete(request: DocumentPosition): CompletionList
}

trait DefinitionProvider {
  def gotoDefinition(parameters: DocumentPosition): Seq[FileRange]
}

case class ReferencesParams(textDocument: TextDocumentIdentifier, position: Position, context: ReferenceContext)

trait ReferencesProvider {
  def references(location: ReferencesParams): collection.Seq[FileRange]
}

object PositionFormat {
  implicit object PositionOrdering extends Ordering[Position] {

    private val ordering = Ordering.by[Position, (Int, Int)](x => (x.line, x.character))
    override def compare(x: Position, y: Position): Int = {
      ordering.compare(x, y)
    }
  }
  implicit val format: OFormat[Position] = Json.format[Position]
}

object SourceRangeFormat {
  implicit val positionFormat = PositionFormat.format
  implicit val format = Json.format[SourceRange]
}

object Diagnostic {
  implicit val rangeFormat = SourceRangeFormat.format
  implicit val fileRangeFormat = Json.format[FileRange]
  implicit val relatedInformation = Json.format[RelatedInformation]
  implicit val format = Json.format[Diagnostic]
}
case class Diagnostic(range: SourceRange, severity: Option[Int],
                      message: String, code: Option[String] = None, source: Option[String] = None,
                      relatedInformation: Seq[RelatedInformation] = Seq.empty) {
  def identifier = Diagnostic(range, None, message, None, None, Seq.empty)
}

case class RelatedInformation(location: FileRange, message: String)

object TextEditFormat {
  implicit val rangeFormat = SourceRangeFormat.format
  implicit val format = Json.format[TextEdit]
}

object WorkspaceEdit {
  implicit val f = TextEditFormat.format
  implicit val format = Json.format[WorkspaceEdit]
}
/**
  * A workspace edit represents changes to many resources managed
  * in the workspace.
  */
case class WorkspaceEdit(changes: Map[String, collection.Seq[TextEdit]])

object CodeActionContext {
  implicit val format = Json.format[CodeActionContext]
}
case class CodeActionContext(diagnostics: Seq[Diagnostic], only: Option[Seq[String]])
case class CodeActionParams(textDocument: TextDocumentIdentifier, range: SourceRange, context: CodeActionContext)

object CodeAction {
  implicit val format = Json.format[CodeAction]
}

case class CodeAction(title: String, kind: String,
                      diagnostics: Option[Seq[Diagnostic]],
                      edit: Option[WorkspaceEdit])

trait CodeActionProvider {
  def getCodeActions(parameters: CodeActionParams): Seq[CodeAction]
}

trait LanguageClient {
  def sendDiagnostics(diagnostics: PublishDiagnostics): Unit
  def trackMetric(name: String, value: Double): Unit
}

trait LanguageServer {
  def textDocumentSync: Int = TextDocumentSyncKind.Full
  def setClient(client: LanguageClient): Unit

  def initialize(parameters: InitializeParams): Unit
  def didOpen(parameters: TextDocumentItem): Unit
  def didClose(parameters: TextDocumentIdentifier): Unit
  def didSave(parameters: DidSaveTextDocumentParams): Unit
  def didChange(parameters: DidChangeTextDocumentParams): Unit
  def initialized(): Unit
}

/**
  * Corresponds to an LSP Location
  */
case class FileRange(uri: String, range: SourceRange) {
  def contains(filePosition: FilePosition): Boolean = {
    uri == filePosition.uri && range.contains(filePosition.position)
  }
}

object FileRange {
  implicit val rangeFormat = SourceRangeFormat.format
  implicit val format = Json.format[FileRange]
  def fromOffsetRange(text: ParseText, offsetRange: FileOffsetRange) = FileRange(offsetRange.uri, offsetRange.range.toRange(text))
}

object DiagnosticSeverity {
  final val Error = 1
  final val Warning = 2
  final val Information = 3
  final val Hint = 4
}



/**
  * A reference to a command.
  *
  * @param title The title of the command, like 'Save'
  * @param command The identifier of the actual command handler
  * @param arguments The arugments this command may be invoked with
  */
case class Command(title: String, command: String, arguments: Seq[Any])


case class TextDocumentIdentifier(uri: String)
object TextDocumentIdentifier { implicit val format = Json.format[TextDocumentIdentifier] }

case class VersionedTextDocumentIdentifier(uri: String, version: Long)
object VersionedTextDocumentIdentifier { implicit val format = Json.format[VersionedTextDocumentIdentifier] }

/**
  * An item to transfer a text document from the client to the
  * server.
  */
case class TextDocumentItem(
                             uri: String,
                             languageId: String,
                             /**
                               * The version number of this document (it will strictly increase after each
                               * change, including undo/redo).
                               */
                             version: Long,
                             text: String)

object TextDocumentItem {
  implicit val format = Json.format[TextDocumentItem]
}

object CompletionItemKind {
  final val Text = 1
  final val Method = 2
  final val Function = 3
  final val Constructor = 4
  final val Field = 5
  final val Variable = 6
  final val Class = 7
  final val Interface = 8
  final val Module = 9
  final val Property = 10
  final val Unit = 11
  final val Value = 12
  final val Enum = 13
  final val Keyword = 14
  final val Snippet = 15
  final val Color = 16
  final val File = 17
  final val Reference = 18
}

case class CompletionItem(
                           label: String,
                           kind: Option[Int] = None,
                           detail: Option[String] = None,
                           documentation: Option[String] = None,
                           sortText: Option[String] = None,
                           filterText: Option[String] = None,
                           insertText: Option[String] = None,
                           textEdit: Option[String] = None,
                           data: Option[String] = None) // An data entry field that is preserved on a completion item between
// a [CompletionRequest](#CompletionRequest) and a [CompletionResolveRequest]
//   (#CompletionResolveRequest)

object CompletionItem {
  implicit def format = Json.format[CompletionItem]
}

trait MarkedString

case class RawMarkedString(language: String, value: String) extends MarkedString {
  def this(value: String) {
    this("text", value)
  }
}

case class MarkdownString(contents: String) extends MarkedString

object MarkedString {
  implicit val reads: Reads[MarkedString] =
    Json.reads[RawMarkedString].map(x => x: MarkedString).orElse(Json.reads[MarkdownString].map(x => x: MarkedString))

  implicit val writes: Writes[MarkedString] = Writes[MarkedString] {
    case raw: RawMarkedString => Json.writes[RawMarkedString].writes(raw)
    case md: MarkdownString => Json.writes[MarkdownString].writes(md)
  }
}


case class ParameterInformation(label: String, documentation: Option[String])

case class SignatureInformation(label: String, documentation: Option[String], parameters: Seq[ParameterInformation])

/**
  * Signature help represents the signature of something
  * callable. There can be multiple signature but only one
  * active and only one active parameter.
  */
case class SignatureHelp(
                          /** One or more signatures. */
                          signatures: Seq[SignatureInformation],

                          /** The active signature. */
                          activeSignature: Option[Int],

                          /** The active parameter of the active signature. */
                          activeParameter: Option[Int])

object ReferenceContext {
  implicit val referenceContext: OFormat[ReferenceContext] = Json.format
}
/**
  * Value-object that contains additional information when
  * requesting references.
  */
case class ReferenceContext(
                             /** Include the declaration of the current symbol. */
                             includeDeclaration: Boolean)

object DocumentHighlightKind {
  /**
    * A textual occurrence.
    */
  final val Text = 1

  /**
    * Read-access of a symbol, like reading a variable.
    */
  final val Read = 2

  /**
    * Write-access of a symbol, like writing to a variable.
    */
  final val Write = 3
}

/**
  * A document highlight is a range inside a text document which deserves
  * special attention. Usually a document highlight is visualized by changing
  * the background color of its range.
  */
case class DocumentHighlight(
                              /** The range this highlight applies to. */
                              range: SourceRange,

                              /** The highlight kind, default is [text](#DocumentHighlightKind.Text). */
                              kind: Int = DocumentHighlightKind.Text)

object SymbolKind {
  final val File = 1
  final val Module = 2
  final val Namespace = 3
  final val Package = 4
  final val Class = 5
  final val Method = 6
  final val Property = 7
  final val Field = 8
  final val Constructor = 9
  final val Enum = 10
  final val Interface = 11
  final val Function = 12
  final val Variable = 13
  final val Constant = 14
  final val String = 15
  final val Number = 16
  final val Boolean = 17
  final val Array = 18
}

case class SymbolInformation(name: String, kind: Int, location: FileRange, containerName: Option[String])
object SymbolInformation {
  implicit val format = Json.format[SymbolInformation]
}

/**
  * The parameters of a [WorkspaceSymbolRequest](#WorkspaceSymbolRequest).
  */
case class WorkspaceSymbolParams(query: String)

/**
  * A code lens represents a [command](#Command) that should be shown along with
  * source text, like the number of references, a way to run tests, etc.
  *
  * A code lens is _unresolved_ when no command is associated to it. For performance
  * reasons the creation of a code lens and resolving should be done to two stages.
  */
case class CodeLens(
                     /**
                       * The range in which this code lens is valid. Should only span a single line.
                       */
                     range: SourceRange,

                     /**
                       * The command this code lens represents.
                       */
                     command: Option[Command],

                     /**
                       * An data entry field that is preserved on a code lens item between
                       * a [CodeLensRequest](#CodeLensRequest) and a [CodeLensResolveRequest]
                       * (#CodeLensResolveRequest)
                       */
                     data: Option[Any])

/**
  * Value-object describing what options formatting should use.
  */
case class FormattingOptions(
                              /**
                                * Size of a tab in spaces.
                                */
                              tabSize: Int,

                              /**
                                * Prefer spaces over tabs.
                                */
                              insertSpaces: Boolean,

                              /**
                                * Signature for further properties.
                                */
                              params: Map[String, Any] // [key: string]: boolean | number | string;
                            )

trait TextDocument {
  /**
    * The associated URI for this document. Most documents have the __file__-scheme, indicating that they
    * represent files on disk. However, some documents may have other schemes indicating that they are not
    * available on disk.
    */
  val uri: String

  /** The identifier of the language associated with this document. */
  val languageId: String

  /**
    * The version number of this document (it will strictly increase after each
    * change, including undo/redo).
    */
  def version: Long

  /**
    * Get the text of this document.
    *
    * @return The text of this document.
    */
  def getText(): String

  /**
    * Converts a zero-based offset to a position.
    *
    * @param offset A zero-based offset.
    * @return A valid [position](#Position).
    */
  def positionAt(offset: Int): Position

  /**
    * Converts the position to a zero-based offset.
    *
    * The position will be [adjusted](#TextDocument.validatePosition).
    *
    * @param position A position.
    * @return A valid zero-based offset.
    */
  def offsetAt(position: Position): Int;

  /** The number of lines in this document. */
  def lineCount: Int;
}

case class TextDocumentChangeEvent(document: TextDocument)

/**
  * An event describing a change to a text document. If range and rangeLength are omitted
  * the new text is considered to be the full content of the document.
  */
case class TextDocumentContentChangeEvent(
                                           /**
                                             * The range of the document that changed.
                                             */
                                           range: Option[SourceRange],

                                           /**
                                             * The length of the range that got replaced.
                                             */
                                           rangeLength: Option[Int],

                                           /**
                                             * The new text of the document.
                                             */
                                           text: String
                                         )

object TextDocumentContentChangeEvent {
  implicit val sourceRangeFormat = SourceRangeFormat.format
  implicit val format = Json.format[TextDocumentContentChangeEvent]
}
