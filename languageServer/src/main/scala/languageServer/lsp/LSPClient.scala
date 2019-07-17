package languageServer.lsp

import com.dhpcs.jsonrpc.JsonRpcMessage.{CorrelationId, NumericCorrelationId}
import languageServer._
import play.api.libs.json.{Json, OFormat, Reads}

import scala.concurrent.Promise

class LSPClient(languageClient: LanguageClient, connection: JsonRpcConnection) {

  val simpleConnection = new SimpleJsonRpcHandler(connection)
  var correlationId = 0

  simpleConnection.addNotificationHandler[PublishDiagnostics](LSPProtocol.diagnostics, notification => {
    languageClient.sendDiagnostics(notification)
  })(Json.format)

  def listen(): Unit = {
    connection.listen()
  }

  def getCorrelationId: CorrelationId = {
    val result = correlationId
    correlationId += 1
    NumericCorrelationId(result)
  }

  def rename(parameters: RenameParams): Promise[WorkspaceEdit] = {
    implicit val editContext: OFormat[TextEdit] = Json.format
    simpleConnection.sendRequest[RenameParams, WorkspaceEdit](
      LSPProtocol.rename, getCorrelationId, parameters)(Json.format, Json.format[WorkspaceEdit])
  }

  def documentSymbol(parameters: DocumentSymbolParams): Promise[Seq[SymbolInformation]] = {
    simpleConnection.sendRequest[DocumentSymbolParams, Seq[SymbolInformation]](
      LSPProtocol.documentSymbol, getCorrelationId, parameters)(Json.format, Reads.of[Seq[SymbolInformation]])
  }

  def references(parameters: ReferencesParams): Promise[Seq[Location]] = {
    implicit val referenceContext: OFormat[ReferenceContext] = Json.format
    simpleConnection.sendRequest[ReferencesParams, Seq[Location]](
      LSPProtocol.references, getCorrelationId, parameters)(Json.format, Reads.of[Seq[Location]])
  }

  def codeAction(parameters: CodeActionParams): Promise[Seq[CodeAction]] = {
    implicit val textEdit: OFormat[TextEdit] = Json.format
    implicit val workspaceEdit: OFormat[WorkspaceEdit] = Json.format
    implicit val codeActionContext: OFormat[CodeActionContext] = Json.format
    implicit val codeAction: OFormat[CodeAction] = Json.format
    simpleConnection.sendRequest[CodeActionParams, Seq[CodeAction]](
      LSPProtocol.codeAction, getCorrelationId, parameters)(Json.format, Reads.of[Seq[CodeAction]])
  }

  def gotoDefinition(parameters: DocumentPosition): Promise[Seq[Location]] = {
    simpleConnection.sendRequest[DocumentPosition, Seq[Location]](
      LSPProtocol.definition, getCorrelationId, parameters)(Json.format, Reads.of[Seq[Location]])
  }

  def complete(parameters: DocumentPosition): Promise[CompletionList] = {
    simpleConnection.sendRequest[DocumentPosition, CompletionList](
      LSPProtocol.completion, getCorrelationId, parameters)(Json.format, Json.format)
  }

  def initialize(parameters: InitializeParams): Promise[InitializeResult] = {
    simpleConnection.sendRequest[InitializeParams, InitializeResult](
      LSPProtocol.initialize, getCorrelationId, parameters)(Json.format, Json.format)
  }

  def didOpen(parameters: TextDocumentItem): Unit = {
    simpleConnection.sendNotification[DidOpenTextDocumentParams](LSPProtocol.didOpen, DidOpenTextDocumentParams(parameters))(Json.format)
  }

  def didClose(identifier: TextDocumentIdentifier): Unit = {
    simpleConnection.sendNotification[DidCloseTextDocumentParams](LSPProtocol.didOpen, DidCloseTextDocumentParams(identifier))(Json.format)
  }

  def didSave(params: DidSaveTextDocumentParams): Unit = {
    simpleConnection.sendNotification[DidSaveTextDocumentParams](LSPProtocol.didSave, params)(Json.format)
  }

  def didChange(parameters: DidChangeTextDocumentParams): Unit = {
    simpleConnection.sendNotification[DidChangeTextDocumentParams](LSPProtocol.didChange, parameters)(Json.format)
  }

  def initialized(): Unit = {
    ??? //simpleConnection.sendNotification[DidChangeTextDocumentParams](LSPProtocol.initialized, parameters)(Json.format)
  }
}
