package application.compilerBuilder

import java.awt.BorderLayout
import java.awt.event.{ActionEvent, MouseEvent}
import javax.swing._
import javax.swing.event.ListSelectionEvent

import application.StyleSheet
import core.deltas.Delta
import org.jdesktop.swingx.JXList

object DeltaInstance
{
  implicit class ParticleLike(val particleLike: Any)
  {
    def getParticle: Delta = particleLike match {
      case particle: Delta => particle
      case instance: DeltaInstance => instance.delta
    }

    def getParticleInstance: DeltaInstance = particleLike match {
      case particle: Delta => new DeltaInstance(particle)
      case instance: DeltaInstance => instance
    }
  }
}

class DeltaInstance(val delta: Delta)

class DeltaInstanceJXList() extends JXList() {
  override def getToolTipText(event: MouseEvent): String = {
    val index = this.locationToIndex(event.getPoint)
    val model = this.getModel
    if (index >= 0)
      model.getElementAt(index).asInstanceOf[DeltaInstance].delta.description
    else
      ""
  }
}

object SelectedDeltasPanel {
  def getPanel(panel: LanguageWorkbench, compilerParticles: DefaultListModel[DeltaInstance]): JPanel = {
    val compilerList = new DeltaInstanceJXList()
    compilerList.setTransferHandler(new SelectedDeltasTransferHandler(compilerList, compilerParticles))
    compilerList.setDropMode(DropMode.INSERT)
    compilerList.setModel(compilerParticles)
    compilerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
    val compilerListPanel = panel.getInjectorListVisuals(compilerList)
    val titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Selected")
    titledBorder.setTitleFont(StyleSheet.defaultFont)
    compilerListPanel.setBorder(titledBorder)

    val removeButton = new JButton("Remove")
    removeButton.setFont(StyleSheet.defaultFont)
    compilerList.addListSelectionListener((e: ListSelectionEvent) =>
      removeButton.setEnabled(compilerList.getSelectedValues.nonEmpty))

    removeButton.addActionListener((e: ActionEvent) => {
      for (selectedValue <- compilerList.getSelectedValues)
        compilerParticles.removeElement(selectedValue)
    })
    compilerListPanel.add(removeButton, BorderLayout.PAGE_END)
    compilerListPanel
  }
}