package com.ssu.xyvento.sihapp.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ssu.xyvento.sihapp.databinding.ActivityChatScreenBinding
import com.ssu.xyvento.sihapp.model.TTSWebSocketClient

class chat_screen_Activity : AppCompatActivity() {
    private val binding: ActivityChatScreenBinding by lazy {
        ActivityChatScreenBinding.inflate(layoutInflater)
    }

    // ✅ TTS Client
    private val ttsClient = TTSWebSocketClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // ✅ WebSocket connect
        ttsClient.connect()

        setupChatSend()
    }

    // 🔊 Jab user text send kare → TTS bole
    private fun setupChatSend() {
        binding.sendButton.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()

            if (text.isNotEmpty()) {
                // Send text to TTS server
                ttsClient.sendText(text)

                // Optional: clear input
                binding.messageInput.text?.clear()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsClient.disconnect()
    }
}
