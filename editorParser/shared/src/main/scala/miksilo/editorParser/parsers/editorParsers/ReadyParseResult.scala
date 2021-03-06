package miksilo.editorParser.parsers.editorParsers

import ParseResults._
import miksilo.editorParser.parsers.core.{OffsetPointer, TextPointer}

case class RecursionsList[State, SeedResult, +Result](
  recursions: List[RecursiveParseResult[State, SeedResult, Result]],
  rest: ParseResults[State, Result])

trait LazyParseResult[State, +Result] {
  def offset: OffsetPointer
  def flatMapReady[NewResult](f: ReadyParseResult[State, Result] => ParseResults[State, NewResult],
                              uniform: Boolean, maxListDepth: Int): ParseResults[State, NewResult]

  def mapReady[NewResult](f: ReadyParseResult[State, Result] => ReadyParseResult[State, NewResult], uniform: Boolean): LazyParseResult[State, NewResult]

  def score: Double = history.score

  def history: History
  def map[NewResult](f: Result => NewResult): LazyParseResult[State, NewResult]

  def mapWithHistory[NewResult](f: ReadyParseResult[State, Result] => ReadyParseResult[State, NewResult],
                                oldHistory: History): LazyParseResult[State, NewResult]
}

class DelayedParseResult[State, Result](val initialOffset: OffsetPointer,
                                        val history: History, _getResults: () => ParseResults[State, Result])
  extends LazyParseResult[State, Result] {

  override def toString = s"$score delayed: $history"

  override def map[NewResult](f: Result => NewResult): DelayedParseResult[State, NewResult] = {
    new DelayedParseResult(initialOffset, history, () => this.getResults.map(f))
  }

  var _results: ParseResults[State, Result] = _
  def getResults = {
    if (_results == null) {
      _results = _getResults()
    }
    _results
  }

  override def mapWithHistory[NewResult](f: ReadyParseResult[State, Result] => ReadyParseResult[State, NewResult], oldHistory: History) =
    new DelayedParseResult(initialOffset, this.history ++ oldHistory, () => {
      val intermediate = this.getResults
      intermediate.mapWithHistory(f, oldHistory)
    })

  override def mapReady[NewResult](f: ReadyParseResult[State, Result] => ReadyParseResult[State, NewResult], uniform: Boolean): DelayedParseResult[State, NewResult] =
    new DelayedParseResult(initialOffset, this.history, () => {
      val intermediate = this.getResults
      intermediate.mapReady(f, uniform)
    })

  override def flatMapReady[NewResult](f: ReadyParseResult[State, Result] => ParseResults[State, NewResult], uniform: Boolean, maxListDepth: Int) =
    singleResult(new DelayedParseResult(initialOffset, this.history, () => {
      val intermediate = this.getResults
      intermediate.flatMapReady(f, uniform, maxListDepth)
    }))

  override def offset: OffsetPointer = if (_results != null) _results.latestRemainder else initialOffset
}

case class RecursiveParseResult[State, SeedResult, +Result](
  get: ParseResults[State, SeedResult] => ParseResults[State, Result]) {

  def compose[NewResult](f: ParseResults[State, Result] => ParseResults[State, NewResult]):
    RecursiveParseResult[State, SeedResult, NewResult] = {

    RecursiveParseResult[State, SeedResult, NewResult](r => f(get(r)))
  }
}

case class ReadyParseResult[State, +Result](resultOption: Option[Result], remainder: TextPointer, state: State, history: History)
  extends LazyParseResult[State, Result] {

  override def map[NewResult](f: Result => NewResult): ReadyParseResult[State, NewResult] = {
    ReadyParseResult(resultOption.map(f), remainder, state, history)
  }

  override def mapWithHistory[NewResult](f: ReadyParseResult[State, Result] => ReadyParseResult[State, NewResult], oldHistory: History) = {
    val newReady = f(this)
    ReadyParseResult(newReady.resultOption, newReady.remainder, newReady.state, newReady.history ++ oldHistory)
  }

  override def mapReady[NewResult](f: ReadyParseResult[State, Result] => ReadyParseResult[State, NewResult], uniform: Boolean):
    ReadyParseResult[State, NewResult] = f(this)

  override def flatMapReady[NewResult](f: ReadyParseResult[State, Result] => ParseResults[State, NewResult], uniform: Boolean, maxListDepth: Int) = f(this)

  override def offset = remainder
}