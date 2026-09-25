package com.example.codexusagewidget

import java.net.*
import java.util.Base64
import java.util.UUID
import java.util.concurrent.Executors

/** Loopback CONNECT tunnel: TLS remains end-to-end inside the official runtime. */
class PhoneNetworkProxy : AutoCloseable {
    private val server = ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"))
    private val pool = Executors.newCachedThreadPool()
    private val secret = UUID.randomUUID().toString()
    val url = "http://widget:$secret@127.0.0.1:${server.localPort}"
    init {
        pool.execute {
            while (!server.isClosed) {
                val socket = try { server.accept() } catch (_: Exception) { break }
                pool.execute { tunnel(socket) }
            }
        }
    }
    private fun tunnel(client: Socket) {
        client.use {
            try {
                client.soTimeout = 30000
                val input = client.getInputStream()
                val bytes = ArrayList<Byte>()
                while (bytes.size < 8192) {
                    val next = input.read()
                    if (next < 0) return
                    bytes.add(next.toByte())
                    if (bytes.size >= 4 && bytes.takeLast(4) == listOf<Byte>(13,10,13,10)) break
                }
                val header = bytes.toByteArray().toString(Charsets.US_ASCII)
                val lines = header.split("\r\n")
                val target = lines.first().split(" ")
                val auth = "Basic " + Base64.getEncoder().encodeToString("widget:$secret".toByteArray())
                val authenticated = lines.any { it.substringBefore(":").equals("Proxy-Authorization", true) && it.substringAfter(":").trim() == auth }
                val authority = target.getOrNull(1) ?: ""
                val host = authority.substringBeforeLast(":").lowercase()
                val allowed = host == "chatgpt.com" || host == "openai.com" || host.endsWith(".openai.com") || host.endsWith(".chatgpt.com")
                if (!authenticated || target.firstOrNull() != "CONNECT" || !authority.endsWith(":443") || !allowed) {
                    client.getOutputStream().write("HTTP/1.1 403 Forbidden\r\n\r\n".toByteArray())
                    return
                }
                Socket().use { remote ->
                    remote.connect(InetSocketAddress(host, 443), 20000)
                    remote.soTimeout = 60000
                    client.getOutputStream().write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray())
                    val upload = pool.submit {
                        try { input.copyTo(remote.getOutputStream()) } catch (_: Exception) {}
                        try { remote.shutdownOutput() } catch (_: Exception) {}
                    }
                    try { remote.getInputStream().copyTo(client.getOutputStream()) } catch (_: Exception) {}
                    upload.cancel(true)
                }
            } catch (_: Exception) {}
        }
    }
    override fun close() { server.close(); pool.shutdownNow() }
}

