package core.parsers.strings

trait CommonParserWriter extends StringParserWriter {

  def identifier: EditorParser[String] =
    elem(Character.isJavaIdentifierStart, "identifier start") ~
      (elem(Character.isJavaIdentifierPart(_: Char), "identifier part")*) ^^ (t => (t._1 :: t._2).mkString)

  /** An integer, without sign or with a negative sign. */
  def wholeNumber: EditorParser[String] =
    """-?\d+""".r
  /** Number following one of these rules:
    *
    *  - An integer. For example: `13`
    *  - An integer followed by a decimal point. For example: `3.`
    *  - An integer followed by a decimal point and fractional part. For example: `3.14`
    *  - A decimal point followed by a fractional part. For example: `.1`
    */
  def decimalNumber: EditorParser[String] =
    """(\d+(\.\d*)?|\d*\.\d+)""".r
  /** Double quotes (`"`) enclosing a sequence of:
    *
    *  - Any character except double quotes, control characters or backslash (`\`)
    *  - A backslash followed by another backslash, a single or double quote, or one
    *    of the letters `b`, `f`, `n`, `r` or `t`
    *  - `\` followed by `u` followed by four hexadecimal digits
    */
  def stringLiteral: EditorParser[String] =
    RegexParser(""""([^"\x00-\x1F\x7F\\]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*""".r).map(r => r.substring(1, r.length)) ~< "\""

  /** A number following the rules of `decimalNumber`, with the following
    *  optional additions:
    *
    *  - Preceded by a negative sign
    *  - Followed by `e` or `E` and an optionally signed integer
    *  - Followed by `f`, `f`, `d` or `D` (after the above rule, if both are used)
    */
  def floatingPointNumber: EditorParser[String] =
    """-?(\d+(\.\d*)?|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?""".r
}
