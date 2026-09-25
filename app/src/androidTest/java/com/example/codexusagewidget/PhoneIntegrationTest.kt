package com.example.codexusagewidget

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class PhoneIntegrationTest {
    @Test fun runtimeCanRequestOfficialDeviceLogin() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val runtime = CodexPhoneRuntime.get(context)
        val account = runtime.call("account/read")
        assertTrue(account.has("account"))
        if (account.isNull("account")) {
            val response = runtime.call("account/login/start", JSONObject().put("type", "chatgptDeviceCode"))
            assertEquals("https://auth.openai.com/codex/device", response.getString("verificationUrl"))
            assertTrue(response.getString("userCode").isNotBlank())
            runtime.call("account/login/cancel", JSONObject().put("loginId", response.getString("loginId")))
        }
    }
}

