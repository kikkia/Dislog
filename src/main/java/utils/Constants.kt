package utils

object Constants {
    @JvmStatic
    val MAX_LOG_RETRIES = 5
    val RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining"
    val RATE_LIMIT_RESET_TIME = "X-RateLimit-Reset"
    val POLL_RATE: Long = 100
}