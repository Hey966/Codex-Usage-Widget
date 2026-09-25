package com.example.codexusagewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class UsageWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        updateAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            UsageDataRepository(context).status("等待更新…")
            UsageRefresh.schedule(context, immediate = true)
            updateAll(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.codexusagewidget.REFRESH"
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, UsageWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val data = UsageDataRepository(context).read()
            val views = RemoteViews(context.packageName, R.layout.usage_widget).apply {
                setTextViewText(R.id.five_percent, data.fiveHourRemaining?.let { "$it%" } ?: "—")
                setTextViewText(R.id.week_percent, data.weeklyRemaining?.let { "$it%" } ?: "—")
                setProgressBar(R.id.five_progress, 100, data.fiveHourRemaining ?: 0, false)
                setProgressBar(R.id.week_progress, 100, data.weeklyRemaining ?: 0, false)
                setTextViewText(R.id.five_reset, "↻ ${data.fiveHourReset}")
                setTextViewText(R.id.week_reset, "↻ ${data.weeklyReset}")
                setTextViewText(R.id.updated, "${data.status}\nUpdated ${data.updatedAt}")
                setOnClickPendingIntent(R.id.refresh, PendingIntent.getBroadcast(
                    context, 0, Intent(context, UsageWidgetProvider::class.java).setAction(ACTION_REFRESH),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
                setOnClickPendingIntent(R.id.widget_title, PendingIntent.getActivity(
                    context, 1, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ))
            }
            manager.updateAppWidget(ids, views)
        }
    }
}


