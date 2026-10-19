package com.example.jarvis

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var logView: TextView
    private lateinit var scroll: ScrollView
    private lateinit var input: EditText
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val voiceLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!text.isNullOrBlank()) handleInput(text)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logView = findViewById(R.id.logView)
        scroll = findViewById(R.id.scroll)
        input = findViewById(R.id.input)
        tts = TextToSpeech(this, this)

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val text = input.text.toString().trim()
            input.setText("")
            if (text.isNotEmpty()) handleInput(text)
        }
        findViewById<Button>(R.id.btnMic).setOnClickListener { startVoice() }

        log("SYSTEM", "JARVIS ready. Type a command or tap Mic.")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.UK
            ttsReady = true
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }

    private fun startVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to JARVIS")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (e: Exception) {
            log("SYSTEM", "Voice input is not available on this device.")
        }
    }

    private fun handleInput(text: String) {
        log("YOU", text)
        log("SYSTEM", "Thinking...")

        Thread {
            val result = runCatching {
                val json = GeminiClient.ask(BuildConfig.GEMINI_API_KEY, BuildConfig.GEMINI_MODEL, text)
                Triple(
                    json.optString("action", "none"),
                    json.optJSONObject("args") ?: JSONObject(),
                    json.optString("reply", "")
                )
            }
            runOnUiThread {
                result.onFailure { log("SYSTEM", "Error: ${it.message}") }
                result.onSuccess { (action, args, reply) ->
                    val outcome = PhoneActions.run(this, action, args)
                    speak(listOf(reply, outcome).filter { it.isNotBlank() }.joinToString(" "))
                }
            }
        }.start()
    }

    private fun speak(text: String) {
        log("JARVIS", text)
        if (ttsReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis")
    }

    private fun log(who: String, text: String) {
        logView.append("$who: $text\n\n")
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }
}
