package com.github.brandtjo.releasescripthelper.ui

import com.github.brandtjo.releasescripthelper.model.ReleaseScript
import com.github.brandtjo.releasescripthelper.settings.ProjectLevelConfigurable
import com.github.brandtjo.releasescripthelper.settings.ProjectLevelState
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selected
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class BasicAddDialog(private val releaseScript: ReleaseScript, private val currentProject: Project) :
    DialogWrapper(true) {

    lateinit var useCustomScriptNumber: CellBuilder<JBCheckBox>
    lateinit var scriptNumber: CellBuilder<JBTextField>
    lateinit var useTicketCheckBox: CellBuilder<JBCheckBox>
    lateinit var ticketType: CellBuilder<ComboBox<String>>
    lateinit var ticketNumber: CellBuilder<JBTextField>
    lateinit var description: CellBuilder<JBTextField>
    lateinit var fileEndings: CellBuilder<ComboBox<String>>

    override fun createButtonsPanel(buttons: MutableList<out JButton>): JPanel {
        val link = ActionLink("Settings") {
            val edited = ShowSettingsUtil.getInstance()
                .editConfigurable(currentProject, ProjectLevelConfigurable(currentProject))
            if (edited) {
                releaseScript.options = ProjectLevelState.getInstanceFor(currentProject).options
                updateUiComponentsByHandBecauseSwingIsStupid()
            }
        }
        val linkAndButtons = ArrayList<JButton>(buttons)
        linkAndButtons.add(0, link)
        return super.createButtonsPanel(linkAndButtons)
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("") {

                useCustomScriptNumber = checkBox("Use Custom Number", releaseScript.options::useCustomScriptNumber)
                scriptNumber = textField(releaseScript::scriptNumber)
                    .enableIf(useCustomScriptNumber.selected)
                    .focused()
                scriptNumber.component.toolTipText =
                    "a custom release script number instead of a unix timestamp"
            }
            row("") {
                useTicketCheckBox = checkBox("For Ticket", releaseScript.options::useTicket)
                ticketType =
                    comboBox(
                        DefaultComboBoxModel(releaseScript.options.ticketTypes),
                        { releaseScript.ticketType },
                        { releaseScript.ticketType = it ?: "" }
                    )
                        .enableIf(useTicketCheckBox.selected)
                ticketType.enabled(releaseScript.options.ticketTypes.size > 1)
                ticketType.component.toolTipText = "selects the type of the ticket"
                ticketNumber = textField(releaseScript::ticketNumber)
                    .enableIf(useTicketCheckBox.selected)
                    .constraints(CCFlags.growX, CCFlags.pushX)
                    .focused()
                ticketNumber.component.toolTipText =
                    "a ticket number with a supported type prefix will automatically adjust the ticket type selection"
                ticketNumber.component.addKeyListener(object : KeyAdapter() {
                    override fun keyReleased(e: KeyEvent) {
                        releaseScript.ticketType =
                            if (ticketType.component.item != null) ticketType.component.item else ""
                        releaseScript.ticketNumber =
                            if (ticketNumber.component.text != null) ticketNumber.component.text else ""
                        ticketNumber.component.text = releaseScript.ticketNumber
                        ticketType.component.item = releaseScript.ticketType
                    }
                })
            }
            row("Description:") {
                description = textField(releaseScript::description)
                    .focused()
                description.component.toolTipText =
                    "the description text is used for the filename and a comment in the file itself"
            }
            row("File Ending:") {
                fileEndings =
                    comboBox(
                        DefaultComboBoxModel(releaseScript.options.fileEndings),
                        { releaseScript.fileEnding },
                        { releaseScript.fileEnding = it ?: "" }
                    )
                fileEndings.enabled(releaseScript.options.fileEndings.size > 1)
            }
        }
    }

    init {
        init()
        title = "Release Script Generation"
    }

    private fun updateUiComponentsByHandBecauseSwingIsStupid() {
        useCustomScriptNumber.component.isSelected = releaseScript.options.useCustomScriptNumber
        if (!releaseScript.options.useCustomScriptNumber) {
            scriptNumber.component.text = ""
        }

        ticketType.component.removeAllItems()
        releaseScript.options.ticketTypes.forEach { ticketType.component.addItem(it) }
        if (!releaseScript.options.ticketTypes.contains(ticketType.component.selectedItem)) {
            ticketType.component.selectedItem =
                if (releaseScript.options.ticketTypes.isNotEmpty()) releaseScript.options.ticketTypes.get(0) else "OCT"
        }

        fileEndings.component.removeAllItems()
        releaseScript.options.fileEndings.forEach { fileEndings.component.addItem(it) }
        if (!releaseScript.options.fileEndings.contains(fileEndings.component.selectedItem)) {
            fileEndings.component.selectedItem =
                if (releaseScript.options.fileEndings.isNotEmpty()) releaseScript.options.fileEndings.get(0) else "sql"
        }
    }
}
