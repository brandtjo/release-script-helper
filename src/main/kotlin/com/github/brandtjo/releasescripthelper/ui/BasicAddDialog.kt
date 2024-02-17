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
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Align.Companion
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

private const val TICKET_NUMBER_DESCRIPTION =
    "a ticket number with a supported type prefix will automatically adjust the ticket type selection"

class BasicAddDialog(private val releaseScript: ReleaseScript, private val currentProject: Project) :
    DialogWrapper(true) {
    lateinit var useCustomScriptNumber: Cell<JBCheckBox>
    lateinit var scriptNumber: Cell<JBTextField>
    lateinit var useTicketCheckBox: Cell<JBCheckBox>
    lateinit var ticketType: Cell<ComboBox<String>>
    lateinit var ticketNumber: Cell<JBTextField>
    lateinit var description: Cell<JBTextField>
    lateinit var fileEndings: Cell<ComboBox<String>>

    override fun createButtonsPanel(buttons: MutableList<out JButton>): JPanel {
        val link =
            ActionLink("Settings") {
                val edited =
                    ShowSettingsUtil.getInstance()
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
                useCustomScriptNumber =
                    checkBox("Use custom number")
                        .bindSelected(releaseScript.options::useCustomScriptNumber)
                scriptNumber =
                    textField()
                        .bindText(releaseScript::scriptNumber)
                        .accessibleDescription(
                            "a custom release script number instead of a unix timestamp",
                        )
                        .enabledIf(useCustomScriptNumber.selected)
                        .align(Align.FILL)
                        .resizableColumn()
                        .focused()
            }
            row("") {
                useTicketCheckBox =
                    checkBox("For ticket")
                        .bindSelected(releaseScript.options::useTicket)
                ticketType =
                    comboBox(releaseScript.options.ticketTypes)
                        .bindItem(releaseScript::ticketType)
                        .enabledIf(useTicketCheckBox.selected)
                ticketType.enabled(releaseScript.options.ticketTypes.size > 1)
                ticketType.component.toolTipText = "selects the type of the ticket"
                ticketNumber =
                    textField()
                        .bindText(releaseScript::ticketNumber)
                        .accessibleDescription(TICKET_NUMBER_DESCRIPTION)
                        .enabledIf(useTicketCheckBox.selected)
                        .align(Align.FILL)
                        .resizableColumn()
                        .focused()
                ticketNumber.component.addKeyListener(
                    object : KeyAdapter() {
                        override fun keyReleased(e: KeyEvent) {
                            releaseScript.ticketType =
                                if (ticketType.component.item != null) ticketType.component.item else ""
                            releaseScript.ticketNumber =
                                if (ticketNumber.component.text != null) ticketNumber.component.text else ""
                            ticketNumber.component.text = releaseScript.ticketNumber
                            ticketType.component.item = releaseScript.ticketType
                        }
                    },
                )
            }
            row("Description:") {
                description =
                    textField()
                        .bindText(releaseScript::description)
                        .accessibleDescription(
                            "the description text is used for the filename and a comment in the file itself",
                        )
                        .align(Companion.FILL)
                        .resizableColumn()
                        .columns(COLUMNS_LARGE)
                        .focused()
            }
            row("File Ending:") {
                fileEndings =
                    comboBox(releaseScript.options.fileEndings)
                        .bindItem(releaseScript::fileEnding)
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
