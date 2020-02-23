package com.kikkia.dislog.exceptions

import com.kikkia.dislog.models.LogLevel

class UrlNotSetException(message: String, level: LogLevel) : Exception(message)