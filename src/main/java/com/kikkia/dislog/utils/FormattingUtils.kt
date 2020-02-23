package com.kikkia.dislog.utils

import com.kikkia.dislog.models.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Class filled with static helpers to help with log formatting
 */
object FormattingUtils {

    /**
     * Generates a log body for a given log, with options
     */
    @JvmStatic
    fun generateLogBody(log: Log, printStackTrace: Boolean, hostIdentifier: String, timeZoneFormat: ZoneOffset): String {
        val timestamp = getTimestamp(timeZoneFormat)

        var body = "`$timestamp` `" + hostIdentifier + ":` " + log.message
        if (log.exception != null) {
            body += "\n`${log.exception!!.message}`\n"
            if (printStackTrace) {
                // Hack to get stack trace as a string
                val sw = StringWriter()
                log.exception!!.printStackTrace(PrintWriter(sw))
                val exceptionAsString = sw.toString()

                body += "```$exceptionAsString```\n"
            }
        }

        // If MDC is populated then tack that on
        if (log.mdc != null) {
            body += "\nMDC:```"
            for ((key, value) in log.mdc) {
                body += "$key: {\n  $value\n}\n"
            }
            body += "```"
        }

        var chunkedBody = body.chunked(1950)[0]

        // TODO: Allow for truncation in other messages
        // TODO: Allow for enable/disable mdc and mdc in another message
        if (chunkedBody.length >= 1950) {
            // If odd number of ``` then close it off
            if ((chunkedBody.count{"```".contains(it)} % 2) == 1) {
                chunkedBody += "```"
            }
            chunkedBody += "\n**Truncated due to length**"
        }

        return "$chunkedBody\n_ _"
    }

    /**
     * Gets the timestamp with date time formatting for the log
     */
    @JvmStatic
    private fun getTimestamp(timeZoneFormat: ZoneOffset) : String {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                .withZone(timeZoneFormat)
                .format(Instant.now())
    }
}