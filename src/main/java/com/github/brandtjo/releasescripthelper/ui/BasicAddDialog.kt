package com.github.brandtjo.releasescripthelper.ui

import com.github.brandtjo.releasescripthelper.model.Options
import com.github.brandtjo.releasescripthelper.model.ReleaseScript
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selected
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class BasicAddDialog(private val releaseScript: ReleaseScript) : DialogWrapper(true) {

    override fun createCenterPanel(): JComponent {
        return panel {
            row("") {
                val useTicketCheckBox = checkBox("For Ticket", releaseScript.options::useTicket)

                val ticketType = comboBox(DefaultComboBoxModel(releaseScript.options.ticketTypes), releaseScript::ticketType)
                    .enableIf(useTicketCheckBox.selected)
                ticketType.component.toolTipText = "selects the type of the ticket"
                val ticketNumber = textField(releaseScript::ticketNumber)
                    .enableIf(useTicketCheckBox.selected)
                    .constraints(CCFlags.pushX)
                    .focused()
                ticketNumber.component.toolTipText =
                    "a ticket number with a supported type prefix will automatically adjust the ticket type selection"
                ticketNumber.component.addKeyListener(object : KeyAdapter() {
                    override fun keyReleased(e: KeyEvent) {
                        releaseScript.ticketNumber = ticketNumber.component.text
                        ticketNumber.component.text = releaseScript.ticketNumber
                        ticketType.component.item = releaseScript.ticketType
                    }
                })
            }
            row("Description:") {
                val description = textField(releaseScript::description)
                description.component.toolTipText =
                    "the description text is used for the filename and a comment in the file itself"
            }
            row("File Ending:") {
                comboBox(DefaultComboBoxModel(releaseScript.options.fileEndings), releaseScript::fileEnding)
                    .enabled(false)
            }
        }
    }

    init {
        init()
        title = "Release Script Options"
    }
}