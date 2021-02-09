package com.github.brandtjo.releasescripthelper.model

class Options {
    var useCustomScriptNumber : Boolean = false
    var useTicket: Boolean = true
    var ticketTypes: Array<String> = arrayOf("OCT", "CHG", "INC")
    var fileEndings: Array<String> = arrayOf("sql")
}