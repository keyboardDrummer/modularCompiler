package miksilo.lspprotocol.lsp

import miksilo.editorParser.parsers.editorParsers.{Position, SourceRange, TextEdit}
import miksilo.lspprotocol.jsonRpc.{JsonRpcConnection, MethodBasedJsonRpcHandler, SerialWorkQueue, WorkItem}
import play.api.libs.json._

import scala.reflect.ClassTag

case class Measurement(name: String, value: Double)
object Measurement {
  implicit val format: OFormat[Measurement] = Json.format[Measurement]
}

class SharedLSPServer(languageServer: LanguageServer,
                connection: JsonRpcConnection,
                workQueue: SerialWorkQueue[WorkItem]) {

  val handler = new MethodBasedJsonRpcHandler(connection)
  connection.setHandler(new LSPServerMessagePreprocessor(handler, workQueue))

  addRequestHandlers()
  addNotificationHandlers()
  languageServer.setClient(new LanguageClientProxy())

  class LanguageClientProxy extends LanguageClient {
    override def sendDiagnostics(diagnostics: PublishDiagnostics): Unit = {
      implicit val publishDiagnosticsFormat: OFormat[PublishDiagnostics] = Json.format
      handler.sendNotification(LSPProtocol.diagnostics, diagnostics)
    }

    override def trackMetric(name: String, value: Double): Unit = {
      handler.sendNotification(LSPProtocol.telemetry, Measurement(name, value))
    }
  }

  def listen(): Unit = {
    connection.listen()
  }

  def addRequestHandlers(): Unit = {

    handler.addRequestHandler[Shutdown, ShutdownResult](LSPProtocol.shutdown, _ => ShutdownResult(0))(Shutdown.format, Json.format)
    handler.addRequestHandler[InitializeParams, InitializeResult](LSPProtocol.initialize, initialize)(Json.format, Json.format)

    def addProvider[Provider: ClassTag, Request, Response](method: String, getHandler: Provider => Request => Response)
                                                          (requestFormat: Reads[Request], responseFormat: Writes[Response]): Unit = {
      languageServer match {
        case provider: Provider =>
          handler.addRequestHandler(method, getHandler(provider))(requestFormat, responseFormat)
        case _ =>
      }
    }

    implicit val positionFormat: OFormat[Position] = PositionFormat.format
    implicit val sourceRangeFormat: OFormat[SourceRange] = SourceRangeFormat.format
    implicit val textDocumentPositionParams: OFormat[DocumentPosition] = Json.format
    implicit val referenceContext: OFormat[ReferenceContext] = Json.format
    implicit val textEdit: OFormat[TextEdit] = Json.format[TextEdit]
    implicit val codeActionContext: OFormat[CodeActionContext] = Json.format
    implicit val codeAction: OFormat[CodeAction] = Json.format
    implicit val markupContentFormat = Json.format[MarkupContent]
    implicit val completionItemFormat = Json.format[CompletionItem]
    implicit val hoverFormat = Json.format[Hover]

    addProvider(LSPProtocol.typeDefinition, (provider: TypeDefinitionProvider) => provider.gotoTypeDefinition)(Json.format, Writes.of[Seq[FileRange]])
    addProvider(LSPProtocol.definition, (provider: DefinitionProvider) => provider.gotoDefinition)(Json.format, Writes.of[Seq[FileRange]])
    addProvider(LSPProtocol.documentSymbol, (provider: DocumentSymbolProvider) => provider.documentSymbols)(Json.format, Writes.of[Seq[SymbolInformation]])
    addProvider(LSPProtocol.references, (provider: ReferencesProvider) => provider.references)(Json.format, Writes.of[collection.Seq[FileRange]])
    addProvider(LSPProtocol.codeAction, (provider: CodeActionProvider) => provider.getCodeActions)(Json.format, Writes.of[Seq[CodeAction]])
    addProvider(LSPProtocol.completion, (provider: CompletionProvider) => provider.complete)(Json.format, Json.format)
    addProvider(LSPProtocol.hover, (provider: HoverProvider) => provider.hover)(Json.format, Writes.of[Option[Hover]])
    addProvider(LSPProtocol.rename, (provider: RenameProvider) => provider.rename)(Json.format, WorkspaceEdit.format)
  }

  def addNotificationHandlers(): Unit = {
    handler.addNotificationHandler(LSPProtocol.exit, (_: ExitNotification) => connection.stop())(ExitNotification.format)
    handler.addNotificationHandler(LSPProtocol.didOpen, (params: DidOpenTextDocumentParams) => languageServer.didOpen(params.textDocument))(Json.format)
    handler.addNotificationHandler(LSPProtocol.didChange, languageServer.didChange)(Json.format)
    handler.addNotificationHandler(LSPProtocol.didClose, (params: DidCloseTextDocumentParams) => languageServer.didClose(params.textDocument))(Json.format)
    handler.addNotificationHandler(LSPProtocol.didSave, languageServer.didSave)(Json.format)

    handler.getNotificationHandlersForMethod(LSPProtocol.initialized).append(_ => languageServer.initialized())
  }

  def initialize(parameters: InitializeParams): InitializeResult = {
    languageServer.initialize(parameters)
    InitializeResult(getCapabilities(parameters.capabilities))
  }

  def getCapabilities(clientCapabilities: ClientCapabilities): ServerCapabilities = {
    ServerCapabilities(
      textDocumentSync = languageServer.textDocumentSync,
      codeActionProvider = languageServer.isInstanceOf[CodeActionProvider],
      documentSymbolProvider = languageServer.isInstanceOf[DocumentSymbolProvider],
      referencesProvider = languageServer.isInstanceOf[ReferencesProvider],
      hoverProvider = languageServer.isInstanceOf[HoverProvider],
      typeDefinitionProvider = languageServer.isInstanceOf[TypeDefinitionProvider],
      definitionProvider = languageServer.isInstanceOf[DefinitionProvider],
      renameProvider = languageServer.isInstanceOf[RenameProvider],
      completionProvider = languageServer match {
        case provider: CompletionProvider => Some(provider.getOptions)
        case _ => None
      })
  }
}
