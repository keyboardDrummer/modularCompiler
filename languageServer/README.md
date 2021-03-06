LanguageServer
=====
A library for implementing language servers. LanguageServer will do most of the heavy lifting involved, leaving language designers free to focus on the language instead of diving into the science of parsing and constraint solving.

Creating language tooling is a hard problem: the topic of many academic publications. Parsers used by interpreters or compilers don't suffice for the editor case, because parsers for editors need to be error correcting, so they continue parsing and still return results when they encounter errors in the input text. Also, parsers for editors must be responsive when the users makes small changes in the text, which requires incremental parsing. There are many tools that help with generating parsers, however it is hard to find those that create parsers suitable for editors. One option is TreeSitter.

Apart from being able to parse code, LSP servers must also understand the static semantics of the language. They should understand how to resolve references to variables and other element of the code. This requires knowing both where the variables and references are, in which scopes they reside, and how the different scopes interact. Analysing types may also be required to resolve references, for example in the case of calls to overloaded methods: we can't know which overload is called until we look at the types.

Miksilo's LanguageServer library abstracts over parsing, static analysis, and exposing the results of these as an LSP server. LanguageServer takes as input only a BNF-like grammar to create the parser, and a sort of simplified interpreter that let's it understand the static semantics of the language. LanguageServer is written in Scala, and defining languages with LanguageServer is done in Scala as well.
