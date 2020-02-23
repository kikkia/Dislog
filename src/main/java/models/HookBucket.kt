package models

import api.DislogClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.json.JSONObject
import utils.Constants
import utils.FormattingUtils
import java.util.*

class HookBucket(private val hookLink: String, private val client: DislogClient) : Thread() {
    private val queue : LinkedList<Log> = LinkedList()
    var remainingRateLimit = 0 // Remaining # of requests we can send before we get ratelimited
    private var rateLimitReset = 0L // Rate limit reset from discord
    var currentLogTries = 0

    fun queueLog(log: Log) {
        queue.add(log)
    }

    override fun run() {
        // Keep polling for when we can send a log
        while (true) {
            val canSend = (remainingRateLimit > 0 || rateLimitReset < System.currentTimeMillis())
            if (canSend) {
                if (queue.size > 0) {
                    if (currentLogTries < Constants.MAX_LOG_RETRIES)
                        sendLog()
                    else {
                        // Drop log that keeps failing to send
                        queue.poll()
                    }
                } else {
                   sleep(100) // No logs present so sleep and wait for more
                }
            } else {
                sleep(rateLimitReset - System.currentTimeMillis()) // Sleep till the rate limit is reset
            }
        }
    }

    private fun sendLog() {
        val log = queue.peek()
        try {
            HttpClients.createDefault().use { client ->
                val url = hookLink
                val jsonObject = JSONObject()

                jsonObject.put("avatar_url", this.client.avatarUrl)
                jsonObject.put("username", this.client.name)
                jsonObject.put("content", FormattingUtils.generateLogBody(log,
                        this.client.printStackTrace,
                        this.client.hostIdentifier,
                        this.client.timeZoneFormat))
                val entity : StringEntity = StringEntity(jsonObject.toString())

                val post : HttpPost = HttpPost(url)
                post.addHeader("Content-type", "application/json")
                post.entity = (entity)
                val response = client.execute(post)

                // Set ratelimiting headers
                remainingRateLimit = response.getFirstHeader(
                                Constants.RATE_LIMIT_REMAINING_HEADER)
                                .value
                                .toInt()

                rateLimitReset = response.getFirstHeader(
                                Constants.RATE_LIMIT_RESET_TIME)
                                .value
                                .toLong() * 1000 // Get in seconds, convert to millis

                if (response.statusLine.statusCode < 200 || response.statusLine.statusCode >= 300) {
                    print("Post to ${log.level} webhook failed with code: " + response.statusLine.statusCode)
                    currentLogTries++
                    return
                }

                currentLogTries = 0
                queue.poll()
            }
        } catch (e: Exception) {
            print("Post to ${log.level} webhook failed with exception: " + e.message)
            currentLogTries++
        }
    }

}