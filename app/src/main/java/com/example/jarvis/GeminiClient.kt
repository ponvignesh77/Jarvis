package com.example.jarvis

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiClient {

    private const val SYSTEM_PROMPT = """
You are JARVIS, a calm, precise AI assistant running on an Android phone.
Reply ONLY with a JSON object containing these keys:
- "reply": a short spoken answer, one or two sentences.
- "action": one of "none", "flashlight_on", "flashlight_off", "open_app", "dial", "set_alarm", "web_search", "open_settings".
- "args": an object. open_app needs {"name"}, dial needs {"number"}, set_alarm needs {"hour" (0-23), "minute" (0-59), "label"}, web_search needs {"query"}. Use {} otherwise.
Use "action": "none" for questions you can answer yourself.
"""

    fun ask(apiKey: String, model: String, userText: String): JSONObject {
        require(apiKey.isNotBlank()) { "Add GEMINI_API_KEY to local.properties and rebuild." }

        val body = JSONObject()
            .put("systemInstruction", JSONObject()
                .put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT))))
            .put("contents", JSONArray().put(JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", userText)))))
            .put("generationConfig", JSONObject().put("responseMimeType", "application/json"))

        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", apiKey)
        }

        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val raw = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) throw RuntimeException("HTTP $code: ${raw.take(200)}")

        val text = JSONObject(raw)
            .getJSONArray("candidates").getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts").getJSONObject(0)
            .getString("text")

        return JSONObject(text)
    }
}
