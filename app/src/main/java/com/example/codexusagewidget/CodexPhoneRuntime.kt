package com.example.codexusagewidget

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.util.Base64
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class CodexPhoneRuntime private constructor(private val context: Context) {
    private val proxy = PhoneNetworkProxy()
    private val pending = ConcurrentHashMap<Int, CompletableFuture<JSONObject>>()
    private val ids = AtomicInteger()
    private val process: Process
    private val writer: java.io.BufferedWriter
    @Volatile var loginCode = ""
    @Volatile var loginUrl = ""
    @Volatile var loginError = ""
    @Volatile var loggingIn = false

    init {
        val home = File(context.noBackupFilesDir, "codex").apply { mkdirs() }
        val certs = File(home, "android-ca.pem")
        val ks = KeyStore.getInstance("AndroidCAStore").apply { load(null) }
        certs.bufferedWriter().use { out ->
            ks.aliases().toList().forEach { alias ->
                out.write("-----BEGIN CERTIFICATE-----\n")
                out.write(Base64.getMimeEncoder(64, byteArrayOf(10)).encodeToString(ks.getCertificate(alias).encoded))
                out.write("\n-----END CERTIFICATE-----\n")
            }
        }
        val builder = ProcessBuilder(
            File(context.applicationInfo.nativeLibraryDir, "libcodex_runtime.so").absolutePath,
            "-c", "cli_auth_credentials_store=\"file\""
        ).directory(home).redirectError(ProcessBuilder.Redirect.to(File("/dev/null")))
        builder.environment().apply {
            put("HOME", home.absolutePath)
            put("CODEX_HOME", home.absolutePath)
            put("TMPDIR", context.cacheDir.absolutePath)
            put("HTTPS_PROXY", proxy.url)
            put("HTTP_PROXY", proxy.url)
            put("SSL_CERT_FILE", certs.absolutePath)
            put("NO_PROXY", "127.0.0.1,localhost")
        }
        process = builder.start()
        writer = process.outputStream.bufferedWriter()
        thread(name = "codex-rpc-reader", isDaemon = true) {
            try {
                process.inputStream.bufferedReader().forEachLine { line ->
                    val msg = try { JSONObject(line) } catch (_: Exception) { return@forEachLine }
                    if (msg.has("id")) pending.remove(msg.optInt("id"))?.complete(msg)
                    if (msg.optString("method") == "account/login/completed") {
                        val params = msg.optJSONObject("params")
                        loggingIn = false
                        loginCode = ""
                        loginUrl = ""
                        if (params?.optBoolean("success") == true) {
                            loginError = ""
                            UsageRefresh.runAsync(context)
                        } else {
                            loginError = "登入未完成，請重新取得登入碼。"
                        }
                    }
                }
            } finally {
                pending.values.forEach { it.completeExceptionally(IllegalStateException("手機服務已停止，請重試")) }
                pending.clear()
            }
        }
        call("initialize", JSONObject().put("clientInfo", JSONObject().put("name", "codex_usage_widget").put("version", "0.2.0")))
        synchronized(writer) { writer.write("{\"method\":\"initialized\"}\n"); writer.flush() }
    }
    fun call(method: String, params: JSONObject = JSONObject()): JSONObject {
        val id = ids.incrementAndGet()
        val future = CompletableFuture<JSONObject>()
        pending[id] = future
        try {
            val request = JSONObject().put("id", id).put("method", method).put("params", params)
            synchronized(writer) { writer.write(request.toString()); writer.newLine(); writer.flush() }
            val message = future.get(60, TimeUnit.SECONDS)
            if (message.has("error")) throw IllegalStateException(message.getJSONObject("error").optString("message", "服務錯誤"))
            return message.optJSONObject("result") ?: JSONObject()
        } finally { pending.remove(id) }
    }
    fun startLogin() {
        if (loggingIn) return
        loginError = ""
        loggingIn = true
        try {
            val result = call("account/login/start", JSONObject().put("type", "chatgptDeviceCode"))
            loginCode = result.getString("userCode")
            loginUrl = result.getString("verificationUrl")
            require(loginUrl == "https://auth.openai.com/codex/device") { "非預期登入網址" }
        } catch (e: Exception) { loggingIn = false; throw e }
    }
    companion object {
        @Volatile private var instance: CodexPhoneRuntime? = null
        @Synchronized fun get(context: Context): CodexPhoneRuntime {
            if (instance?.process?.isAlive != true) {
                instance?.proxy?.close()
                instance = CodexPhoneRuntime(context.applicationContext)
            }
            return instance!!
        }
        fun current() = instance
    }
}


