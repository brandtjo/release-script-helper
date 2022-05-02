package com.github.brandtjo.releasescripthelper.model

class Options {
    var defaultDirectory: String = ""
    var useCustomScriptNumber: Boolean = false
    var useTicket: Boolean = true
    var ticketTypes: List<String> = listOf("oct", "CHG")
    var fileEndings: List<String> = listOf("sql")
}
