package utils

import models.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object FormattingUtils {

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
        return "$body\n_ _"
    }

    @JvmStatic
    private fun getTimestamp(timeZoneFormat: ZoneOffset) : String {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                .withZone(timeZoneFormat)
                .format(Instant.now())
    }
}