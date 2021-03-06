package miksilo.playground.application.compilerCockpit

import java.awt.event.ActionEvent

import javax.swing.JButton
import miksilo.languageServer.core.language.Phase

class ShowPhasesButton(compilerCockpit: LanguageSandbox) extends JButton("Show phases") {
  addActionListener((e: ActionEvent) => {
    val text: String = compilerCockpit.language.compilerPhases.zipWithIndex.
      map(indexedPhase => getIndexedPhaseDescription(indexedPhase)).
      mkString("\n\n")
    compilerCockpit.setOutputText(text)
  })

  def getIndexedPhaseDescription(indexedPhase: (Phase, Int)): String = {
    val phase = indexedPhase._1
    val index = indexedPhase._2
    s"""${index + 1}: ${phase.key}
       |${phase.description}""".stripMargin
  }
}
