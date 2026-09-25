package com.example.codexusagewidget

import android.app.job.*
import android.content.ComponentName
import android.content.Context
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

object UsageRefresh {
    val executor = Executors.newSingleThreadExecutor()
    private val running = AtomicBoolean(false)
    fun runAsync(context: Context) { executor.execute { refresh(context.applicationContext) } }
    fun refresh(context: Context) {
        if (!running.compareAndSet(false, true)) return
        val repo = UsageDataRepository(context)
        try {
            repo.status("更新中…")
            UsageWidgetProvider.updateAll(context)
            val runtime = CodexPhoneRuntime.get(context)
            val account = runtime.call("account/read")
            if (account.isNull("account")) {
                repo.status("請開啟 App 登入")
                return
            }
            repo.saveResponse(runtime.call("account/rateLimits/read"))
        } catch (_: Exception) {
            repo.status("更新失敗，請確認網路或重新登入")
        } finally {
            running.set(false)
            UsageWidgetProvider.updateAll(context)
        }
    }
    fun schedule(context: Context, immediate: Boolean = false) {
        val scheduler = context.getSystemService(JobScheduler::class.java)
        val id = if (immediate) 102 else 101
        if (!immediate && scheduler.getPendingJob(id) != null) return
        val builder = JobInfo.Builder(id, ComponentName(context, UsageJobService::class.java))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        if (immediate) builder.setMinimumLatency(0).setOverrideDeadline(1000)
        else builder.setPeriodic(15 * 60 * 1000L).setPersisted(true)
        if (scheduler.schedule(builder.build()) != JobScheduler.RESULT_SUCCESS)
            UsageDataRepository(context).status("無法排程，請開啟 App 更新")
    }
}

class UsageJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        UsageRefresh.executor.execute {
            UsageRefresh.refresh(applicationContext)
            jobFinished(params, false)
        }
        return true
    }
    override fun onStopJob(params: JobParameters): Boolean = true
}

