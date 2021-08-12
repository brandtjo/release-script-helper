package com.github.brandtjo.releasescripthelper.model

class Options {
    var defaultDirectory: String = "/"
    var useCustomScriptNumber: Boolean = false
    var useTicket: Boolean = true
    var ticketTypes: Array<String> = arrayOf("oct", "CHG", "INC")
    var fileEndings: Array<String> = arrayOf("sql")
}
