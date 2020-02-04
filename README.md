Miksilo [![Build Status](https://travis-ci.org/keyboardDrummer/Miksilo.svg?branch=master)](https://travis-ci.org/keyboardDrummer/Miksilo) [![Join the chat at https://gitter.im/Miksilo/Lobby#](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Miksilo/Lobby#?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============

Miksilo is a [language workbench](https://en.wikipedia.org/wiki/Language_workbench), which is a tool to construct programming languages. Miksilo, whose name comes from the [Esperanto](https://en.wikipedia.org/wiki/Esperanto) word for mixer, lets you create languages quickly by mixing existing languages and building on top of them. Language construction is notoriously hard, and these days involves not just writing a compiler or interpreter, but also editor tooling to provide features such as code completion, inline errors and code navigation. Miksilo takes a declarative language definition and from that generates all the tools expected of a modern language. To learn how to use Miksilo to generate editor tooling, visit [this page](http://keyboarddrummer.github.io/Miksilo/practical/buildLanguageTooling/).

### Repository structure
Miksilo is divided into the following sub-projects:

- [EditorParser](editorParser). Defines a parser API that can be used to create parsers suitable for use in text editors.
- [LSPProtocol](LSPProtocol). Defines the communication layers of an LSP client and an LSP server.
- [LanguageServer](languageServer). Enables starting an LSP server from a language definition.
- [ModularLanguages](modularLanguages). Defines various tools for defining languages in a modular way, and come with many predefined language building blocks.
- [Playground](playground). A desktop UI application that enables constructing languages by combining predefined languages blocks using drag and drop.

### Build instructions

1. Grab [the source](https://github.com/keyboardDrummer/Miksilo) from GitHub
1. Make sure you have installed the Java 8 JDK, or a higher version.
1. Install <a href="http://www.scala-sbt.org/">sbt</a>
1. Call 'sbt playground/run' in the project root to build Miksilo and start the sandbox desktop application.

### Contributions
There's an infinite amount of work to be done for Miksilo, so contributions are very welcome. There are many different topics to work on, some suitable for a Bachelor's or Master's thesis.

Some examples of cool features:
- A DSL for static semantics, such as name binding and type checking. See the paper [A constraint language for static semantic analysis based on scope graphs](http://delivery.acm.org/10.1145/2850000/2847543/p49-antwerpen.pdf?ip=145.129.111.38&id=2847543&acc=OA&key=4D4702B0C3E38B35%2E4D4702B0C3E38B35%2E4D4702B0C3E38B35%2E77FCF3B2F09622E1&CFID=992904318&CFTOKEN=51306518&__acm__=1507451717_5c1e5970ab3ac31fbd9849edb486a802) for inspiration
- Generating syntactic code completion from a grammar, as in the paper [Principled syntactic code completion using placeholders](http://delivery.acm.org/10.1145/3000000/2997374/p163-amorim.pdf?ip=145.129.111.38&id=2997374&acc=OA&key=4D4702B0C3E38B35%2E4D4702B0C3E38B35%2E4D4702B0C3E38B35%2E77FCF3B2F09622E1&CFID=992904318&CFTOKEN=51306518&__acm__=1507451951_eb454d2173854f174d05e3c1e1526bbd)
- Incremental compilation: incremental parsing, incremental type checking, etc.
- Add a new language front-end or back-end.

If you would like to contribute then:
1. Reach out on [the Gitter](https://gitter.im/Miksilo/Lobby), so other contributors can help you out where the documentation is lacking.
1. Look through the open issues to see if you find something interesting
1. Enjoy the work ;-)
1. Once you're done, submit a pull request and make sure the build server approves it.
