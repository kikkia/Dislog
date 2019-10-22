package exceptions

import models.LogLevel

class UrlNotSetException(message: String, level: LogLevel) : Exception(message)