package miksilo.modularLanguages.core.bigrammar.grammars

import miksilo.modularLanguages.core.bigrammar.BiGrammar

class BiChoice(var left: BiGrammar, var right: BiGrammar, val firstIsLonger: Boolean) extends BiGrammar
{
  override def children = Seq(left, right)

  override def withChildren(newChildren: Seq[BiGrammar]) = new BiChoice(newChildren(0), newChildren(1), firstIsLonger)

  override def containsParser(recursive: BiGrammar => Boolean): Boolean = recursive(left) || recursive(right)
}
