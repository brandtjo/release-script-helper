package com.github.brandtjo.releasescripthelper.model

import com.intellij.util.containers.stream

class Options {
    var useTimestamp : Boolean = false
    var useTicket: Boolean = true
    val ticketTypes: Array<String> = arrayOf("OCT", "CHG", "INC")
    val fileEndings: Array<String> = arrayOf("sql")
}