package com.github.brandtjo.releasescripthelper.action

import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selected
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.DefaultComboBoxModel

class ReleaseScriptOptionsDialog : DialogWrapper(true) {

    private val options: ReleaseScriptOptions = ReleaseScriptOptions()

    override fun createCenterPanel(): JComponent {
        return panel {
            row("") {
                val useTicketCheckBox = checkBox("For Ticket", options::useTicket)

                val ticketType = comboBox(DefaultComboBoxModel(options.ticketTypes), options::ticketType)
                        .enableIf(useTicketCheckBox.selected)
                ticketType.component.toolTipText = "selects the type of the ticket"
                val ticketNumber = textField(options::ticketNumber)
                        .enableIf(useTicketCheckBox.selected)
                        .constraints(CCFlags.pushX)
                        .focused()
                ticketNumber.component.toolTipText = "a ticket number with a supported type prefix will automatically adjust the ticket type selection"
                ticketNumber.component.addKeyListener(object : KeyAdapter() {
                    override fun keyReleased(e: KeyEvent) {
                        options.deriveTicketTypeFromTicketNumber(ticketNumber.component.text)
                        ticketNumber.component.text = options.ticketNumber
                        ticketType.component.item = options.ticketType
                    }
                })
            }
            row("Description:") {
                val description = textField(options::description)
                description.component.toolTipText = "the description text is used for the filename and a comment in the file itself"
            }
            row("File Ending:") {
                comboBox(DefaultComboBoxModel(options.fileEndings), options::fileEnding)
                        .enabled(false)
            }
        }
    }

    fun getOptions(): ReleaseScriptOptions {
        return options
    }

    init {
        init()
        title = "Release Script Options"
    }
}