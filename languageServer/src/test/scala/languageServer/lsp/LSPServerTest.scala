package languageServer.lsp

import java.io.ByteArrayOutputStream

import langserver.types._
import languageServer.{CompletionProvider, DefinitionProvider, LanguageServer}
import org.scalatest.AsyncFunSpec

class LSPServerTest extends AsyncFunSpec {

  val initialize: String = """Content-Length: 304
                             |
                             |{"jsonrpc":"2.0","id":0,"method":"initialize","params":{"rootUri":"file:///local/home/rwillems/workspaces/cloud9-dev/ide-assets/src/AWSCloud9Core/plugins/c9.ide.language.languageServer.lsp/worker/test_files/project","capabilities":{"workspace":{"applyEdit":false},"textDocument":{"definition":true}},"trace":"verbose"}}""".stripMargin.replace("\n", "\r\n")

  val expectedInitializeResult: String = """Content-Length: 440
                                           |
                                           |{"jsonrpc":"2.0","result":{"capabilities":{"textDocumentSync":1,"hoverProvider":false,"completionProvider":{"resolveProvider":false,"triggerCharacters":[]},"definitionProvider":true,"referencesProvider":false,"documentHighlightProvider":false,"documentSymbolProvider":false,"workspaceSymbolProvider":false,"codeActionProvider":false,"documentFormattingProvider":false,"documentRangeFormattingProvider":false,"renameProvider":false}},"id":0}""".
    stripMargin.replace("\n","\r\n")

  case class ServerAndClient(client: LSPClient, server: LSPServer,
                             clientOut: ByteArrayOutputStream,
                             serverOut: ByteArrayOutputStream)

  it("can initialize") {

    val languageServer = new DefaultLanguageServer {}
    val serverAndClient = setupServerAndClient(languageServer)

    val serverOutExpectation =
      """Content-Length: 371
        |
        |{"jsonrpc":"2.0","result":{"capabilities":{"textDocumentSync":1,"hoverProvider":false,"definitionProvider":false,"referencesProvider":false,"documentHighlightProvider":false,"documentSymbolProvider":false,"workspaceSymbolProvider":false,"codeActionProvider":false,"documentFormattingProvider":false,"documentRangeFormattingProvider":false,"renameProvider":false}},"id":0}""".stripMargin
    val clientOutExpectation =
      """Content-Length: 99
        |
        |{"jsonrpc":"2.0","method":"initialize","params":{"rootUri":"someRootUri","capabilities":{}},"id":0}""".stripMargin
    val initializePromise = serverAndClient.client.initialize(InitializeParams(None, "someRootUri", ClientCapabilities()))
    initializePromise.future.map(result => {
      assert(result.capabilities == serverAndClient.server.getCapabilities)
      assertResult(fixNewlines(clientOutExpectation))(serverAndClient.clientOut.toString)
      assertResult(fixNewlines(serverOutExpectation))(serverAndClient.serverOut.toString)
    })
  }

  it("can use goto definition") {
    val document = TextDocumentItem("a","",0,"content")
    val request = DocumentPosition(TextDocumentIdentifier(document.uri), Position(0, 0))
    val definitionRange = Range(Position(0, 1), Position(1, 2))

    val languageServer: LanguageServer = new DefaultLanguageServer with DefinitionProvider {
      override def gotoDefinition(parameters: DocumentPosition): Seq[Location] = {
        if (parameters == request)
          return Seq(Location(parameters.textDocument.uri, definitionRange))
        Seq.empty
      }
    }

    val serverAndClient = setupServerAndClient(languageServer)
    val client = serverAndClient.client
    val gotoPromise = client.gotoDefinition(request)

    val serverOutExpectation =
      """Content-Length: 121
        |
        |{"jsonrpc":"2.0","result":[{"uri":"a","range":{"start":{"line":0,"character":1},"end":{"line":1,"character":2}}}],"id":0}""".stripMargin
    val clientOutExpectation =
      """Content-Length: 133
        |
        |{"jsonrpc":"2.0","method":"textDocument/definition","params":{"textDocument":{"uri":"a"},"position":{"line":0,"character":0}},"id":0}""".stripMargin
    gotoPromise.future.map(result => {
      assert(result == Seq(Location(document.uri, definitionRange)))
      assertResult(fixNewlines(clientOutExpectation))(serverAndClient.clientOut.toString)
      assertResult(fixNewlines(serverOutExpectation))(serverAndClient.serverOut.toString)
    })
  }

  it("can use completion") {
    val document = TextDocumentItem("a","",0, "content")
    val request = DocumentPosition(TextDocumentIdentifier(document.uri), Position(0, 0))

    val languageServer: LanguageServer = new DefaultLanguageServer with CompletionProvider {
      override def getOptions: CompletionOptions = CompletionOptions(resolveProvider = false, Seq.empty)

      override def complete(request: DocumentPosition): CompletionList =
        CompletionList(isIncomplete = false, Seq(CompletionItem("hello")))
    }

    val serverAndClient = setupServerAndClient(languageServer)
    val client = serverAndClient.client
    val completePromise = client.complete(request)

    val serverOutExpectation =
      """Content-Length: 84
        |
        |{"jsonrpc":"2.0","result":{"isIncomplete":false,"items":[{"label":"hello"}]},"id":0}""".stripMargin
    val clientOutExpectation =
      """Content-Length: 133
        |
        |{"jsonrpc":"2.0","method":"textDocument/completion","params":{"textDocument":{"uri":"a"},"position":{"line":0,"character":0}},"id":0}""".stripMargin
    completePromise.future.map(result => {
      assert(result.items == Seq(CompletionItem("hello")))
      assertResult(fixNewlines(clientOutExpectation))(serverAndClient.clientOut.toString)
      assertResult(fixNewlines(serverOutExpectation))(serverAndClient.serverOut.toString)
    })

  }

  def fixNewlines(text: String): String = text.replace("\n","\r\n")

  private def setupServerAndClient(languageServer: LanguageServer): ServerAndClient = {
    val clientToServer = new InOutStream()
    val serverToClient = new InOutStream()

    val serverOutCopy = new ByteArrayOutputStream()
    val clientOutCopy = new ByteArrayOutputStream()
    val clientOut = new StreamMultiplexer(Seq(clientToServer.out, clientOutCopy))
    val serverOut = new StreamMultiplexer(Seq(serverToClient.out, serverOutCopy))
    val serverConnection = new JsonRpcConnection(clientToServer.in, serverOut)
    val server = new LSPServer(languageServer, serverConnection)
    val client = new LSPClient(new JsonRpcConnection(serverToClient.in, clientOut))
    new Thread(() => server.listen()).start()
    new Thread(() => client.listen()).start()
    ServerAndClient(client, server, clientOutCopy, serverOutCopy)
  }

  class DefaultLanguageServer extends LanguageServer {
    override def didOpen(parameters: TextDocumentItem): Unit = {}

    override def didChange(parameters: DidChangeTextDocumentParams): Unit = {}

    override def didClose(parameters: TextDocumentIdentifier): Unit = {}

    override def didSave(parameters: TextDocumentIdentifier): Unit = {}

    override def initialized(): Unit = {}

    override def initialize(parameters: InitializeParams): Unit = {}
  }
}