package lsp

import com.typesafe.scalalogging.Logger
import langserver.messages._
import langserver.types._
import org.slf4j.LoggerFactory

/**
  * A language server implementation. Users should subclass this class and implement specific behavior.
  */
abstract class LanguageServer(connection: Connection) {

  lazy val logger: Logger = Logger(LoggerFactory.getLogger(getClass.getName))

  protected val documentManager = new TextDocumentManager(connection)

  connection.notificationHandlers += {
    case DidOpenTextDocumentParams(td) => onOpenTextDocument(td)
    case DidChangeTextDocumentParams(td, changes) => onChangeTextDocument(td, changes)
    case DidSaveTextDocumentParams(td) => onSaveTextDocument(td)
    case DidCloseTextDocumentParams(td) => onCloseTextDocument(td)
    case DidChangeWatchedFiles(changes) => onChangeWatchedFiles(changes)
    case e => logger.error(s"Unknown notification $e")
  }
  connection.setServer(this)

  def start(): Unit = {
    connection.start()
  }

  def onOpenTextDocument(td: TextDocumentItem) = {
    logger.debug(s"openTextDocuemnt $td")
  }

  def onChangeTextDocument(td: VersionedTextDocumentIdentifier, changes: Seq[TextDocumentContentChangeEvent]) = {
    logger.debug(s"changeTextDocuemnt $td")
  }

  def onSaveTextDocument(td: TextDocumentIdentifier) = {
    logger.debug(s"saveTextDocuemnt $td")
    connection.showMessage(MessageType.Info, s"Saved text document ${td.uri}")
  }

  def onCloseTextDocument(td: TextDocumentIdentifier) = {
    logger.debug(s"closeTextDocuemnt $td")
  }

  def onChangeWatchedFiles(changes: Seq[FileEvent]) = {
    //    ???
  }

  def initialize(pid: Long, rootPath: String, capabilities: ClientCapabilities): ServerCapabilities = {
    logger.info(s"Initialized with $pid, $rootPath, $capabilities")
    ServerCapabilities(completionProvider = Some(CompletionOptions(false, Seq("."))))
  }

  def completionRequest(textDocument: TextDocumentIdentifier, position: Position): ResultResponse = {
    CompletionList(isIncomplete = false, Nil)
  }

  def shutdown(): Unit = {

  }

  def gotoDefinitionRequest(textDocument: TextDocumentIdentifier, position: Position): DefinitionResult

  def hoverRequest(textDocument: TextDocumentIdentifier, position: Position): Hover = {
    Hover(Nil, None)
  }

  def documentSymbols(tdi: TextDocumentIdentifier): Seq[SymbolInformation] = {
    Seq.empty
  }
}