package com.kikkia.dislog.api

import com.kikkia.dislog.models.HookBucket
import com.kikkia.dislog.models.Log
import com.kikkia.dislog.models.LogLevel
import com.kikkia.dislog.utils.Constants
import java.time.ZoneOffset
import java.util.stream.Collectors

/**
 * Dislog client to use for sending logs to discord webhooks
 */
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

    /**
     * Queues the log to be sent to all channels defined for that log level
     * @property log the log to queue to send
     */
    fun queueLog(log: Log) {
        for (bucket in webhookBucketMap[log.level]!!) {
            bucket.queueLog(log)
        }
    }

    /**
     * Builder class for building dislog clients
     */
    class Builder {
        val urlMap: HashMap<LogLevel, MutableList<String>> = HashMap()
        var name = "dislog"
        var avatarUrl = "https://i.imgur.com/SmqNOwu.jpg"
        var hostIdentifier: String = "dislog"
        var printStackTrace: Boolean = false
        var zoneFormat = ZoneOffset.UTC
        var maxRetries: Int = Constants.MAX_LOG_RETRIES
        var pollRate: Long = Constants.POLL_RATE

        /**
         * Sets the username for the webhook
         * @property name the username for the webhook
         * @return The builder object
         */
        fun setUsername(name: String) : Builder {
            this.name = name
            return this
        }

        /**
         * Sets the avatar url for the webhook
         * @property url the url to the avatar
         */
        fun setAvatarUrl(url: String) : Builder {
            this.avatarUrl = url
            return this
        }


        /**
         * Sets the Identifier for the webhook
         * @property identifier the identifier prefix for the webhook
         * @return The builder object
         */
        fun setIdentifier(identifier: String) : Builder {
            this.hostIdentifier = identifier
            return this
        }


        /**
         * Sets whether or not stack trace should be printed for error logs
         * @property print boolean for priting stack traces
         * @return The builder object
         */
        fun printStackTrace(print: Boolean) : Builder {
            this.printStackTrace = print
            return this
        }


        /**
         * Add a webhook to route logs too
         * @property level The log level of logs that will be sent to this webhook
         * @property webhookUrl The url to route logs to
         * @return The builder object
         */
        fun addWebhook(level: LogLevel, webhookUrl: String) : Builder {
            urlMap.computeIfPresent(level) { _, v -> v.plus(webhookUrl).toMutableList()}
            urlMap.computeIfAbsent(level) { mutableListOf(webhookUrl)}
            return this
        }


        /**
         * Sets the number of retries for the webhook
         * @property retries How many times to retry sending a log before dropping it
         * @return The builder object
         */
        fun setMaxRetries(retries: Int) : Builder {
            this.maxRetries = retries
            return this
        }


        /**
         * Sets the pollrate for the webhook thread
         * @property milliRate the period at which the thread for the webhook will poll the queue
         * @return The builder object
         */
        fun setPollRate(milliRate: Long) : Builder {
            this.pollRate = milliRate
            return this
        }


        /**
         * Sets the timezone for the webhook
         * @property zone a timezone offset to use when logging
         * @return The builder object
         */
        fun setTimeZone(zone: ZoneOffset) : Builder {
            this.zoneFormat = zone
            return this
        }


        /**
         * Builds a new dislog client from the builder
         * @return A newly built dislog client
         */
        fun build() : DislogClient {
            return DislogClient(this)
        }
    }
}
