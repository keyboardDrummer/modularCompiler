package core.parsers.core

import util.cache.Cache

import scala.annotation.tailrec
import scala.collection.mutable
import scala.language.higherKinds

trait ParserWriter {

  type Input <: ParseInput
  type ParseResult[+Result] <: ParseResultLike[Result]
  type PN = ParseNode
  type Self[+R] <: Parser[R]
  type PState <: ParseState

  case class ParseNode(input: Input, parser: Parser[Any])

  def succeed[Result](result: Result): Self[Result]
  def fail[Result](message: String): Self[Result]
  def lazyParser[Result](inner: => Self[Result]): Self[Result]

  def failure[Result](input: Input, message: String): ParseResult[Result]

  def choice[Result](first: Self[Result], other: => Self[Result], leftIsAlwaysBigger: Boolean = false): Self[Result]

  def flatMap[Result, NewResult](left: Self[Result], getRight: Result => Self[NewResult]): Self[NewResult]

  def map[Result, NewResult](original: Self[Result], f: Result => NewResult): Self[NewResult]
    //= flatMap(original, (result: Result) => succeed(f(result)))

  def leftRight[Left, Right, NewResult](left: Self[Left],
                                        right: => Self[Right],
                                        combine: (Left, Right) => NewResult): Self[NewResult]
//  = {
//    flatMap(left, (leftResult: Left) => map(right, (rightResult: Right) => combine(leftResult, rightResult)))
//  }

  implicit class ParserExtensions[+Result](parser: Self[Result]) {

    def addAlternative[Other >: Result](getAlternative: (Self[Other], Self[Other]) => Self[Other]): Self[Other] = {
      lazy val result: Self[Other] = lazyParser(parser | getAlternative(parser, result))
      result
    }

    def |[Other >: Result](other: => Self[Other]) = choice(parser, other)

    def ~[Right](right: => Self[Right]) = leftRight(parser, right, (a: Result, b: Right) => (a, b))

    def ~<[Right](right: Self[Right]) = leftRight(parser, right, Processor.ignoreRight[Result, Right])

    def ~>[Right](right: Self[Right]) = leftRight(parser, right, Processor.ignoreLeft[Result, Right])

    def flatMap[NewResult](getRight: Result => Self[NewResult]): Self[NewResult] =
      ParserWriter.this.flatMap(parser, getRight)

    def map[NewResult](f: Result => NewResult): Self[NewResult] = ParserWriter.this.map(parser, f)

    def option: Self[Option[Result]] = choice(this.map(x => Some(x)), succeed[Option[Result]](None))

    def repN(amount: Int): Self[List[Result]] = {
      if (amount == 0) {
        succeed(List.empty[Result])
      } else {
        leftRight[Result, List[Result], List[Result]](parser, repN(amount - 1), (a,b) => a :: b)
      }
    }

    def many[Sum](zero: Sum, reduce: (Result, Sum) => Sum): Self[Sum] = {
      lazy val result: Self[Sum] = choice(leftRight(parser, result, reduce), succeed(zero), leftIsAlwaysBigger = true)
      result
    }

    def * : Self[List[Result]] = {
      many(List.empty, (h: Result, t: List[Result]) => h :: t)
    }

    def ^^[NewResult](f: Result => NewResult) = map(f)

    def manySeparated(separator: Self[Any]): Self[List[Result]] =
      leftRight(parser, (separator ~> parser).*, (h: Result, t: List[Result]) => h :: t) |
        succeed(List.empty)
  }

  trait Parser[+Result] {
    def parseNaively(input: Input, state: PState): ParseResult[Result]
  }

  class ParseState(val resultCache: Cache[ParseNode, ParseResult[Any]]) {

    this: PState =>

    val recursionIntermediates = mutable.HashMap[PN, ParseResult[Any]]()
    val callStackSet = mutable.HashSet[PN]()
    val callStack = mutable.Stack[Parser[Any]]()
    var parsersPartOfACycle: Set[Parser[Any]] = Set.empty
    val parsersWithBackEdges = mutable.HashSet[Parser[Any]]() //TODO possible this can be only the parsers.

    def parseCached[Result](parser: Parser[Result], input: Input): ParseResult[Result] = {
      if (resultCache == null)
        return parser.parseNaively(input, this) //TODO find a nicer way

      val node = ParseNode(input, parser)
      resultCache.get(node).getOrElse({
        val value = parseIteratively(parser, input)
        if (!parsersPartOfACycle.contains(parser)) {
          resultCache.add(node, value)
        }
        value
      }).asInstanceOf[ParseResult[Result]]
    }

    def parseIteratively[Result](parser: Parser[Result], input: Input): ParseResult[Result] = {
      if (resultCache == null)
        return parser.parseNaively(input, this) //TODO find a nicer way

      val node = ParseNode(input, parser)
      getPreviousResult(node) match {
        case None =>

          callStackSet.add(node)
          callStack.push(node.parser)
          var result = parser.parseNaively(input, this)
          if (result.successful && parsersWithBackEdges.contains(parser)) {
            result = growResult(node, parser, result, this)
          }
          callStackSet.remove(node)
          callStack.pop()
          result

        case Some(result) => result
      }
    }

    @tailrec
    private def growResult[Result](node: PN, parser: Parser[Result], previous: ParseResult[Result], state: PState): ParseResult[Result] = {
      recursionIntermediates.put(node, previous)

      val nextResult: ParseResult[Result] = parser.parseNaively(node.input, state)
      nextResult.getSuccessRemainder match {
        case Some(remainder) if remainder.offset > previous.getSuccessRemainder.get.offset =>
          growResult(node, parser, nextResult, state)
        case _ =>
          recursionIntermediates.remove(node)
          previous
      }
    }

    def getPreviousResult[Result](node: PN): Option[ParseResult[Result]] = {
      if (callStackSet.contains(node)) {
        parsersWithBackEdges.add(node.parser)
        val index = callStack.indexOf(node.parser)
        parsersPartOfACycle ++= callStack.take(index + 1)
        return Some(recursionIntermediates.getOrElse(node,
          failure[Result](node.input, "Traversed back edge without a previous result")).
          asInstanceOf[ParseResult[Result]])
      }
      None
    }
  }

  class Lazy[+Result](_inner: => Parser[Result]) extends Parser[Result] {
    lazy val inner: Parser[Result] = _inner

    override def parseNaively(input: Input, state: PState): ParseResult[Result] = inner.parseNaively(input, state)
  }

  trait ParseResultLike[+Result] {
    def getSuccessRemainder: Option[Input]
    def successful: Boolean = getSuccessRemainder.nonEmpty
    def get: Result
    def map[NewResult](f: Result => NewResult): ParseResult[NewResult]
  }

  class MapParser[Result, NewResult](original: Parser[Result], f: Result => NewResult) extends Parser[NewResult] {
    override def parseNaively(input: Input, state: PState) = {
      val result = state.parseCached(original, input)
      result.map(f)
    }
  }
}

object Processor {
  def ignoreLeft[Left, Right](left: Left, right: Right): Right = right
  def ignoreRight[Left, Right](left: Left, right: Right): Left = left
}

trait ParseInput {
  def offset: Int
  def atEnd: Boolean
}