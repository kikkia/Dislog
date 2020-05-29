package com.kikkia.dislog.models

import org.slf4j.MDC
import java.lang.Exception

/**
 * Represents a log that can be used to send to discord
 */
data class Log(var message: String, var level: LogLevel, var exception: Exception?) {
    val mdc = MDC.getCopyOfContextMap()
}