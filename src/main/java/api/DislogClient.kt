package api

import exceptions.UrlNotSetException
import models.Log
import models.LogLevel
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.json.JSONObject
import org.slf4j.MDC

class DislogClient private constructor(builder: DislogClient.Builder){

    val webhookUrlMap: HashMap<LogLevel, String> = HashMap()
    val name: String?
    val avatarUrl: String?
    val hostIdentifier: String?
    var printStackTrace: Boolean = false

    init {
        this.webhookUrlMap.putAll(builder.urlMap)
        this.name = builder.name
        this.avatarUrl = builder.avatarUrl
        this.hostIdentifier = builder.hostIdentifier
        this.printStackTrace = builder.printStackTrace
    }

    fun sendLog(log: Log): Boolean {
        try {
            HttpClients.createDefault().use { client ->
                val url = webhookUrlMap[log.level] ?: throw UrlNotSetException("Url for level not set: " + log.level, log.level)
                val jsonObject = JSONObject()

                jsonObject.put("avatar_url", avatarUrl)
                jsonObject.put("username", name)
                jsonObject.put("content", generateLogBody(log))
                val entity : StringEntity = StringEntity(jsonObject.toString())

                val post : HttpPost = HttpPost(url)
                post.addHeader("Content-type", "application/json")
                post.entity = (entity)
                val response = client.execute(post)

                if (response.statusLine.statusCode < 200 || response.statusLine.statusCode >= 300) {
                    print("Post to ${log.level} webhook failed with code: " + response.statusLine.statusCode)
                    return false
                }
            }
        } catch (e: Exception) {
            print("Post to ${log.level} webhook failed with exception: " + e.message)
            return false
        }

        return true
    }

    private fun generateLogBody(log: Log): String {
        var body = "`" + hostIdentifier + ":` " + log.message
        if (log.exception != null) {
            body += "\n`${log.exception!!.message}`\n"
            if (printStackTrace) {
                body += "```${log.exception!!.stackTrace.toString()}```"
            }
        }
        body += "```\n"
        for ((key, value) in MDC.getCopyOfContextMap()) {
            body += "$key: {\n  $value\n}\n"
        }
        body += "```"
        return body
    }

    class Builder {
        var urlMap: HashMap<LogLevel, String> = HashMap()
        var name = "dislog"
          private set
        var avatarUrl = "https://i.imgur.com/SmqNOwu.jpg"
          private set
        var hostIdentifier: String? = null
          private set
        var printStackTrace: Boolean = false
          private set

        fun setUsername(name: String) = apply { this.name = name }
        fun setAvatarUrl(url: String) = apply { this.avatarUrl = avatarUrl }
        fun setHostIdentifier(identifier: String) = apply { this.hostIdentifier = identifier }
        fun setPrintStackTrace(print: Boolean) = apply { this.printStackTrace = print }
        fun setErrorWebhookUrl(url: String) = apply { this.urlMap[LogLevel.ERROR] = url }
        fun setWarnWebhookUrl(url: String) = apply { this.urlMap[LogLevel.WARN] = url }
        fun setInfoWebhookUrl(url: String) = apply { this.urlMap[LogLevel.INFO] = url }
        fun setDebugWebhookUrl(url: String) = apply { this.urlMap[LogLevel.DEBUG] = url }
    }
}
