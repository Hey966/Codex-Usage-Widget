package com.example.codexusagewidget

import android.appwidget.AppWidgetManager
import android.content.*
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        UsageRefresh.schedule(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var usage by remember { mutableStateOf(UsageDataRepository(this).read()) }
                var code by remember { mutableStateOf("") }
                var loginMessage by remember { mutableStateOf("") }
                var busy by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    while (true) {
                        usage = UsageDataRepository(this@MainActivity).read()
                        CodexPhoneRuntime.current()?.let {
                            code = it.loginCode
                            if (it.loginError.isNotEmpty()) loginMessage = it.loginError
                        }
                        delay(1000)
                    }
                }
                Scaffold { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Codex Usage Widget", style = MaterialTheme.typography.headlineMedium)
                        Text("手機獨立同步 · ${usage.plan}")
                        UsageCard("5 小時", usage.fiveHourRemaining, usage.fiveHourReset)
                        UsageCard("每週", usage.weeklyRemaining, usage.weeklyReset)
                        Text("Updated ${usage.updatedAt}")
                        Text(usage.status)
                        Button(onClick = { UsageRefresh.runAsync(this@MainActivity) }, modifier = Modifier.fillMaxWidth()) { Text("立即刷新") }
                        OutlinedButton(enabled = !busy && code.isEmpty(), onClick = {
                            busy = true
                            loginMessage = "正在取得登入碼…"
                            UsageRefresh.executor.execute {
                                try {
                                    CodexPhoneRuntime.get(this@MainActivity).startLogin()
                                    runOnUiThread { loginMessage = "請在 OpenAI 官方頁面輸入下方代碼，完成後回到此 App。"; busy = false }
                                } catch (e: Exception) {
                                    runOnUiThread { loginMessage = "登入連線失敗：" + (e.message ?: "請重試"); busy = false }
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("登入 ChatGPT / Codex") }
                        if (loginMessage.isNotEmpty()) Text(loginMessage)
                        if (code.isNotEmpty()) {
                            Text(code, style = MaterialTheme.typography.headlineMedium)
                            Button(onClick = {
                                getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("OpenAI 登入碼", code))
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://auth.openai.com/codex/device")))
                            }) { Text("複製代碼並開啟官方登入頁") }
                        }
                        val manager = AppWidgetManager.getInstance(this@MainActivity)
                        if (manager.isRequestPinAppWidgetSupported) {
                            OutlinedButton(onClick = {
                                manager.requestPinAppWidget(ComponentName(this@MainActivity, UsageWidgetProvider::class.java), null, null)
                            }) { Text("加入桌面 Widget") }
                        }
                        Text("約每 15 分鐘更新，省電模式可能延後。失敗時保留最後成功的資料；不提供的額度顯示 —。")
                        Text("手機整合驗證版：使用官方 Codex 0.157.0 執行環境。登入憑證只存於本 App 私有空間，不備份到雲端。")
                        TextButton(onClick = {
                            UsageRefresh.executor.execute {
                                try {
                                    CodexPhoneRuntime.get(this@MainActivity).call("account/logout")
                                    UsageDataRepository(this@MainActivity).clear()
                                    UsageWidgetProvider.updateAll(this@MainActivity)
                                } catch (_: Exception) { UsageDataRepository(this@MainActivity).status("登出未完成，請重試") }
                            }
                        }) { Text("登出並清除額度資料") }
                    }
                }
            }
        }
    }
}
@Composable
private fun UsageCard(title: String, remaining: Int?, reset: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(remaining?.let { "$it% 剩餘" } ?: "—", style = MaterialTheme.typography.headlineLarge)
            LinearProgressIndicator(progress = { (remaining ?: 0) / 100f }, modifier = Modifier.fillMaxWidth())
            Text("↻ $reset")
        }
    }
}

