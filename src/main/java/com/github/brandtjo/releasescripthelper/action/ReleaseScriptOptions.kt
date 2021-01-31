package com.github.brandtjo.releasescripthelper.action

import com.intellij.util.containers.stream

class ReleaseScriptOptions {
    var useTicket: Boolean = true
    val ticketTypes: Array<String> = arrayOf("OCT", "CHG", "INC")
    val fileEndings: Array<String> = arrayOf("sql")

    var ticketType: String = "OCT"
    var ticketNumber: String = ""

    var description: String = ""
    var fileEnding = "sql";

    fun deriveTicketTypeFromTicketNumber(rawTicketNumber: String) {
        val matchingType = ticketTypes.stream().filter { rawTicketNumber.toLowerCase().startsWith(it.toLowerCase()) }.findAny()
        if(matchingType.isPresent) {
            ticketType = matchingType.get();
            ticketNumber = rawTicketNumber.replaceFirst(matchingType.get(), "", true)
        } else {
            ticketNumber = rawTicketNumber
        }
    }
}