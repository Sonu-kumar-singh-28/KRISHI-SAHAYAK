package com.ssu.xyvento.sihapp.model

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class TTSWebSocketClient {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var audioTrack: AudioTrack? = null

    private val request = Request.Builder()
        .url("wss://luise-ungarnered-wailfully.ngrok-free.dev/ws/stream")
        .build()

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("✅ WebSocket Connected")
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            playAudioChunk(bytes.toByteArray())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            println("🔌 WebSocket Closing")
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?
        ) {
            println("❌ WebSocket Error: ${t.message}")
        }
    }

    fun connect() {
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendText(text: String) {
        webSocket?.send(text)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client closed")
        audioTrack?.release()
        audioTrack = null
    }

    private fun playAudioChunk(data: ByteArray) {
        if (audioTrack == null) initAudioTrack()
        audioTrack?.write(data, 0, data.size)
        audioTrack?.play()
    }

    // ✅ ONLY ONE initAudioTrack (NEW API)
    private fun initAudioTrack() {
        val sampleRate = 24000

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(bufferSize)
            .build()
    }
}
