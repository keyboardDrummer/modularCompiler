package deltas.javac.trivia

import core.bigrammar._
import core.bigrammar.grammars._
import core.deltas.grammars.LanguageGrammars
import core.deltas.{DeltaWithGrammar, Language, NodeGrammar}

//noinspection ZeroIndexToHead
object TriviaInsideNode extends DeltaWithGrammar {

  override def description: String = "Moves trivia grammars left of a node to the inside of the node"

  def debugPrint(value: String): Unit = {
    System.out.println(value)
  }

  override def transformGrammars(grammars: LanguageGrammars, language: Language): Unit = {
    var visited = Set.empty[BiGrammar]
    val descendants = grammars.root.descendants.sortBy(ref => ref.toString())
    val descString = descendants.toString()
    val expected = """List( <INSIDE> TriviaGrammar,  <INSIDE> TriviaGrammar,  <INSIDE> TriviaGrammar,  <INSIDE> TriviaGrammar,  <INSIDE> | TriviaInsideNodeTest.IntegerClass, (number).As(TriviaInsideNodeTest.Value) <INSIDE> (number).As(TriviaInsideNodeTest.Value), (number).As(TriviaInsideNodeTest.Value) <INSIDE> TriviaInsideNodeTest.IntegerClass, + <INSIDE> TriviasGrammar +, BodyGrammar <INSIDE> BodyGrammar TriviasGrammar, BodyGrammar TriviasGrammar <INSIDE> BodyGrammar TriviasGrammar, BodyGrammar TriviasGrammar <INSIDE> TriviasGrammar BodyGrammar TriviasGrammar, TriviaGrammar <INSIDE> TriviaGrammar%*, TriviaGrammar <INSIDE> TriviaGrammar%*, TriviaGrammar <INSIDE> TriviaGrammar%*, TriviaGrammar <INSIDE> TriviaGrammar%*, TriviaGrammar%* <INSIDE> TriviasGrammar, TriviaGrammar%* <INSIDE> TriviasGrammar, TriviaGrammar%* <INSIDE> TriviasGrammar, TriviaGrammar%* <INSIDE> TriviasGrammar, TriviaInsideNodeTest.Add <INSIDE> | TriviaInsideNodeTest.IntegerClass | TriviaInsideNodeTest.Add, TriviaInsideNodeTest.Expression <INSIDE> BodyGrammar, TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Left) <INSIDE> TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Left) TriviasGrammar +, TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Left) TriviasGrammar + <INSIDE> TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Left) TriviasGrammar + TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right), TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Left) TriviasGrammar + TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right) <INSIDE> TriviaInsideNodeTest.Add, TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Left) TriviasGrammar + TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right) <INSIDE> TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Left) TriviasGrammar + TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right), TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right) <INSIDE> TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right), TriviaInsideNodeTest.IntegerClass <INSIDE> | TriviaInsideNodeTest.IntegerClass, TriviasGrammar + <INSIDE> TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Left) TriviasGrammar +, TriviasGrammar + <INSIDE> TriviasGrammar +, TriviasGrammar <INSIDE> BodyGrammar TriviasGrammar, TriviasGrammar <INSIDE> TriviasGrammar +, TriviasGrammar <INSIDE> TriviasGrammar BodyGrammar TriviasGrammar, TriviasGrammar <INSIDE> TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right), TriviasGrammar BodyGrammar TriviasGrammar <INSIDE> ProgramGrammar, TriviasGrammar BodyGrammar TriviasGrammar <INSIDE> TriviasGrammar BodyGrammar TriviasGrammar, TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right) <INSIDE> TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Left) TriviasGrammar + TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right), TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right) <INSIDE> TriviasGrammar TriviaInsideNodeTest.Expression.As(TriviaInsideNodeTest.Right), number <INSIDE> (number).As(TriviaInsideNodeTest.Value), | TriviaInsideNodeTest.IntegerClass <INSIDE> | TriviaInsideNodeTest.IntegerClass | TriviaInsideNodeTest.Add, | TriviaInsideNodeTest.IntegerClass | TriviaInsideNodeTest.Add <INSIDE> TriviaInsideNodeTest.Expression)"""
    assert(expected == descString, "desc strings not equal")
    debugPrint("descendants = " + descString)
    for(path <- descendants)
    {
      path.value match {
        case trivia: WithTrivia =>
          if (!visited.contains(path.value)) {
            debugPrint("visiting: " + path)
            visited += path.value

            val grammar = trivia.getGrammar
            debugPrint("grammar = " + grammar.toString)
            if (hasLeftNode(new RootGrammar(grammar))) {
              debugPrint("moving trivia in: " + trivia.toString())
              path.set(trivia.getGrammar)
              injectTrivia(grammars, path, trivia.inner.isInstanceOf[LeftRight])
            }
          }
          else
            debugPrint("skipped: " + path)
        case _ =>
      }
    }
  }

  private def hasLeftNode(path: GrammarPath) = {
    debugPrint("hasLeftNode with: " + path.toString)
    val leftChildren = path.value.getLeftChildren
    debugPrint("leftChildren: " + leftChildren.toString())
    leftChildren.exists(p => p.isInstanceOf[NodeGrammar])
  }

  def injectTrivia(grammars: LanguageGrammars, grammar: GrammarReference, horizontal: Boolean): Unit = {
    debugPrint("inject trivia called for " + grammar.value.toString())
    grammar.value match {
      case sequence: Sequence =>
        val left = sequence.getLeftChildren.drop(1).head
        val child = grammar.children.find(ref => ref.value == left).get
        debugPrint("Sequence")
        injectTrivia(grammars, child, horizontal)
      case _:NodeGrammar =>
        debugPrint("NodeGrammar")
        if (!isLeftRecursive(grammar.children.head)) {
          placeTrivia(grammars, grammar.children.head, horizontal)
        }
      case _:Choice =>
        debugPrint("Choice")
        injectTrivia(grammars, grammar.children(0), horizontal)
        injectTrivia(grammars, grammar.children(1), horizontal)
      case _:WithTrivia => //TODO if we consider the grammars as a graph and only move WithTrivia's from all incoming edges at once, then we wouldn't need this hack.
        debugPrint("WithTrivia")
      case _:BiFailure =>
        debugPrint("BiFailure")
      case _ =>
        debugPrint("fallback")
        if (grammar.children.length == 1)
          injectTrivia(grammars, grammar.children.head, horizontal)
        else placeTrivia(grammars, grammar, horizontal)
    }
  }

  def placeTrivia(grammars: LanguageGrammars, grammar: GrammarReference, horizontal: Boolean): Unit = {
    debugPrint("maybe placing trivia in " + grammar.value.toString)
    if (!grammar.value.isInstanceOf[WithTrivia] && grammar.value.containsParser()) {
      debugPrint("actually placing")
      grammar.set(new WithTrivia(grammar.value, grammars.trivia, horizontal))
    }
  }

  def isLeftRecursive(grammar: GrammarPath): Boolean = {
    val edges = grammar.ancestors.collect({ case ref: GrammarReference => ref }).map(p => (p.property, p.parent)).toList
    debugPrint("edges = " + edges.toString())
    if (edges.distinct.size != edges.size)
      return true

    grammar.value match {
      case _:Sequence => isLeftRecursive(grammar.children.head)
      case _:NodeGrammar =>
        false
      case _:Choice => isLeftRecursive(grammar.children(0)) || isLeftRecursive(grammar.children(1))
      case _:BiFailure =>
        false
      case _ =>
        if (grammar.children.length == 1)
          isLeftRecursive(grammar.children.head)
        else
          false
    }
  }
}
