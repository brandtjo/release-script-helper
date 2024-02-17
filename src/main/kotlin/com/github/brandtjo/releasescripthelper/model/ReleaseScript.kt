package com.github.brandtjo.releasescripthelper.model

import org.apache.commons.lang3.StringUtils
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Optional
import java.util.stream.Collectors
import java.util.stream.Stream

class ReleaseScript {
    var date: Date = Date()
    var scriptNumber: String = ""
    var ticketType: String? = ""
    var ticketNumber: String = ""
        set(rawTicketNumber) {
            field = rawTicketNumber
            options.ticketTypes.stream()
                .filter { field.lowercase(Locale.getDefault()).startsWith(it.lowercase(Locale.getDefault())) }.findAny()
                .ifPresent {
                    ticketType = it
                    field = field.replaceFirst(it, "", true)
                }
        }
    var description: String = ""
    var content: String? = ""
    var fileEnding: String? = ""
    var options: Options = Options()
        set(options) {
            field = options
            ticketType = if (options.ticketTypes.isNotEmpty()) options.ticketTypes[0] else StringUtils.EMPTY
            fileEnding = if (options.fileEndings.isNotEmpty()) options.fileEndings[0] else StringUtils.EMPTY
        }

    fun getReleaseScriptName(): String {
        val prefix = if (!options.useCustomScriptNumber) date.time.toString() else scriptNumber
        val ticket = parseTicket()
        val description = parseDescription()
        val suffix = parseSuffix()
        return Stream.of(prefix, ticket, description).filter(StringUtils::isNotBlank)
            .collect(Collectors.joining("_")) + '.' + suffix
    }

    fun getReleaseScriptContent(): ByteArray {
        val line1 = "-- description:   $description\n"
        val dateTime = SimpleDateFormat("dd.MM.yyyy HH:mm:ss z", Locale.ENGLISH).format(date)
        val line2 = "-- date:          $dateTime\n"
        val line3 = "\n"
        val script = "$content\n"
        return (line1 + line2 + if (StringUtils.isNotBlank(content)) line3 + script else "").toByteArray(
            StandardCharsets.UTF_8,
        )
    }

    private fun parseTicket(): String? {
        return if (options.useTicket && StringUtils.isNotBlank(ticketType) && StringUtils.isNotBlank(ticketNumber)) {
            ticketType + StringUtils.strip(ticketNumber)
        } else {
            null
        }
    }

    private fun parseDescription(): String {
        val rawDescription = Optional.ofNullable(description)
        if (rawDescription.isPresent && StringUtils.isNotBlank(rawDescription.get())) {
            var description = rawDescription.get()
            description = sanitizeFileNamePart(description, " ") ?: ""
            description = StringUtils.strip(description)
            description = description.replace("\\s+".toRegex(), "-")
            return description.lowercase(Locale.getDefault())
        }
        return getDefaultInstance().description
    }

    private fun parseSuffix(): String? {
        val defaultSuffix = getDefaultInstance().fileEnding
        val suffix = Optional.ofNullable(fileEnding)
        return if (suffix.isPresent && StringUtils.isNotBlank(suffix.get())) {
            StringUtils.defaultIfBlank(
                sanitizeFileNamePart(suffix.get(), "")?.lowercase(Locale.getDefault()),
                defaultSuffix,
            )
        } else {
            defaultSuffix
        }
    }

    private fun sanitizeFileNamePart(
        fileNamePart: String?,
        replacement: String,
    ): String? {
        if (StringUtils.isNotBlank(fileNamePart)) {
            return fileNamePart?.replace("[\\Q<>:\"/\\|?*.,´`+~#-_!§$%&()[]{}^°@€\\E]+".toRegex(), replacement)
        }
        throw IllegalArgumentException("Invalid File Name Part")
    }

    private fun getDefaultInstance(): ReleaseScript {
        val default = ReleaseScript()
        default.options.useTicket = false
        default.description = "release-script"
        default.fileEnding = "sql"
        return default
    }
}
