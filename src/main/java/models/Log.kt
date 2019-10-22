package models

import java.lang.Exception

data class Log(var message: String, var level: LogLevel, var exception: Exception?)