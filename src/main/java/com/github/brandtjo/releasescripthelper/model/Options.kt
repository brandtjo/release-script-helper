package com.github.brandtjo.releasescripthelper.model

class Options {
    var useCustomNumber : Boolean = false
    var useTicket: Boolean = true
    val ticketTypes: Array<String> = arrayOf("OCT", "CHG", "INC")
    val fileEndings: Array<String> = arrayOf("sql")
}