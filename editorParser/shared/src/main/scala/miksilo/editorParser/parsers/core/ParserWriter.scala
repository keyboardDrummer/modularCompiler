package miksilo.editorParser.parsers.core

import scala.language.existentials

trait ParserWriter {

  type Parser[+Result]

  def succeed[Result](result: Result): Parser[Result]

  def choice[Result](first: Parser[Result], other: => Parser[Result], firstIsLonger: Boolean = false): Parser[Result]

  def map[Result, NewResult](original: Parser[Result], f: Result => NewResult): Parser[NewResult]

  implicit class ParserExtensions[+Result](parser: Parser[Result]) {

    def |[Other >: Result](other: => Parser[Other]): Parser[Other] = choice(parser, other)

    def map[NewResult](f: Result => NewResult): Parser[NewResult] = ParserWriter.this.map(parser, f)

    def option: Parser[Option[Result]] = choice(this.map(x => Some(x)), succeed[Option[Result]](None), firstIsLonger = true)

    def ^^[NewResult](f: Result => NewResult): Parser[NewResult] = map(f)
  }
}




