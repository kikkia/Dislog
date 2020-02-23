package com.kikkia.dislog.models

import org.slf4j.MDC
import java.lang.Exception

data class Log(var message: String, var level: LogLevel, var exception: Exception?) {
    val mdc = MDC.getCopyOfContextMap()
}