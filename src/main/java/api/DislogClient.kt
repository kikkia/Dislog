package api

import models.HookBucket
import models.Log
import models.LogLevel
import utils.Constants
import java.time.ZoneOffset
import java.util.stream.Collectors

class DislogClient private constructor(builder: DislogClient.Builder){

    val webhookBucketMap: HashMap<LogLevel, List<HookBucket>> = HashMap()
    val name: String
    val avatarUrl: String
    val hostIdentifier: String
    var printStackTrace: Boolean = false
    var timeZoneFormat: ZoneOffset = ZoneOffset.UTC
    var maxRetries: Int = Constants.MAX_LOG_RETRIES
    var threadPollRate: Long

    init {
        this.name = builder.name
        this.avatarUrl = builder.avatarUrl
        this.hostIdentifier = builder.hostIdentifier
        this.printStackTrace = builder.printStackTrace
        this.timeZoneFormat = builder.zoneFormat
        this.maxRetries = builder.maxRetries
        this.threadPollRate = builder.pollRate

        // Translate Urls to buckets
        for ((key, value) in builder.urlMap) {
            webhookBucketMap[key] = value.stream().map { HookBucket(it, this) }
                    .collect(Collectors.toList())
        }

        // Start all bucket threads
        for ((key, value) in webhookBucketMap) {
            for (bucket in value) {
                bucket.start()
            }
        }
    }

    // Queues the log to be sent to all channels defined for that log level
    fun queueLog(log: Log) {
        for (bucket in webhookBucketMap[log.level]!!) {
            bucket.queueLog(log)
        }
    }

    class Builder {
        val urlMap: HashMap<LogLevel, MutableList<String>> = HashMap()
        var name = "dislog"
        var avatarUrl = "https://i.imgur.com/SmqNOwu.jpg"
        var hostIdentifier: String = "dislog"
        var printStackTrace: Boolean = false
        var zoneFormat = ZoneOffset.UTC
        var maxRetries: Int = Constants.MAX_LOG_RETRIES
        var pollRate: Long = Constants.POLL_RATE

        fun setUsername(name: String) : Builder {
            this.name = name
            return this
        }

        fun setAvatarUrl(url: String) : Builder {
            this.avatarUrl = url
            return this
        }

        fun setIdentifier(identifier: String) : Builder {
            this.hostIdentifier = identifier
            return this
        }

        fun printStackTrace(print: Boolean) : Builder {
            this.printStackTrace = print
            return this
        }

        fun addWebhook(level: LogLevel, webhookUrl: String) {
            urlMap.computeIfPresent(level) { _, v -> v.plus(webhookUrl).toMutableList()}
            urlMap.computeIfAbsent(level) { mutableListOf(webhookUrl)}
        }

        fun setMaxRetries(retries: Int) : Builder {
            this.maxRetries = retries
            return this
        }

        fun setPollRate(milliRate: Long) : Builder {
            this.pollRate = milliRate
            return this
        }

        fun setTimeZone(zone: ZoneOffset) : Builder {
            this.zoneFormat = zone
            return this
        }

        fun build() : DislogClient {
            return DislogClient(this)
        }
    }
}
