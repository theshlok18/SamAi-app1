package dev.shlok.samai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SamTextToSpeech @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(0.95f)
                tts?.setPitch(0.9f)
                isReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { _isSpeaking.value = true }
                    override fun onDone(utteranceId: String?) { _isSpeaking.value = false }
                    @Deprecated("Deprecated")
                    override fun onError(utteranceId: String?) { _isSpeaking.value = false }
                })
            }
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sam_utterance")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun destroy() {
        tts?.shutdown()
        tts = null
    }
}
