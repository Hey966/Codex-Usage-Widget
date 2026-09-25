package com.example.codexusagewidget

import android.content.Context
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class UsageData(
    val fiveHourRemaining: Int?, val weeklyRemaining: Int?,
    val fiveHourResetAt: Long?, val weeklyResetAt: Long?,
    val updatedAtMillis: Long, val status: String, val plan: String
) {
    val updatedAt: String get() = if (updatedAtMillis == 0L) "尚未同步" else
        Instant.ofEpochMilli(updatedAtMillis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    val fiveHourReset get() = resetText(fiveHourResetAt)
    val weeklyReset get() = resetText(weeklyResetAt)
    companion object {
        fun resetText(seconds: Long?): String {
            if (seconds == null) return "未提供"
            val minutes = (seconds - Instant.now().epochSecond + 59) / 60
            if (minutes <= 0) return "待同步確認重置"
            val days = minutes / 1440
            val hours = (minutes % 1440) / 60
            val mins = minutes % 60
            return if (days > 0) "${days}d ${hours}h" else "${hours}h ${mins}m"
        }
    }
}

class UsageDataRepository(context: Context) {
    // Separate storage ensures old random demo values can never appear as live usage.
    private val prefs = context.applicationContext.getSharedPreferences("live_usage_v1", Context.MODE_PRIVATE)
    fun read() = UsageData(
        if (prefs.contains("five")) prefs.getInt("five", 0) else null,
        if (prefs.contains("week")) prefs.getInt("week", 0) else null,
        if (prefs.contains("fiveReset")) prefs.getLong("fiveReset", 0) else null,
        if (prefs.contains("weekReset")) prefs.getLong("weekReset", 0) else null,
        prefs.getLong("updated", 0), prefs.getString("status", "請先登入") ?: "請先登入",
        prefs.getString("plan", "Codex") ?: "Codex"
    )
    fun status(text: String) { prefs.edit().putString("status", text).apply() }
    fun clear() { prefs.edit().clear().apply() }
    fun saveResponse(response: JSONObject) {
        val buckets = response.optJSONObject("rateLimitsByLimitId")
        val bucket = if (buckets != null && buckets.length() > 0) buckets.optJSONObject("codex") else response.optJSONObject("rateLimits")
        require(bucket != null) { "帳號未提供 Codex 額度" }
        val windows = listOfNotNull(bucket.optJSONObject("primary"), bucket.optJSONObject("secondary"))
        val five = windows.firstOrNull { it.optInt("windowDurationMins") == 300 }
        val week = windows.firstOrNull { it.optInt("windowDurationMins") == 10080 }
        require(five != null || week != null) { "尚無 5 小時或每週額度資料" }
        val edit = prefs.edit()
        for ((prefix, window) in listOf("five" to five, "week" to week)) {
            edit.remove(prefix).remove(prefix + "Reset")
            if (window != null) {
                val used = window.optDouble("usedPercent", Double.NaN)
                require(used.isFinite() && used in 0.0..100.0) { "額度格式不正確" }
                edit.putInt(prefix, (100.0 - used).toInt())
                if (!window.isNull("resetsAt") && window.optLong("resetsAt") > 0)
                    edit.putLong(prefix + "Reset", window.getLong("resetsAt"))
            }
        }
        edit.putString("plan", bucket.optString("planType", "Codex").uppercase())
            .putLong("updated", System.currentTimeMillis())
            .putString("status", "已同步").apply()
    }
}

