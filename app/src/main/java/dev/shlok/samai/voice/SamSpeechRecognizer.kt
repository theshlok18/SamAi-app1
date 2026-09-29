package dev.shlok.samai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ListeningState { IDLE, LISTENING, PROCESSING, ERROR }

@Singleton
class SamSpeechRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _listeningState = MutableStateFlow(ListeningState.IDLE)
    val listeningState: StateFlow<ListeningState> = _listeningState

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel

    fun isAvailable() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (!isAvailable()) {
            onError("Speech recognition is not available on this device.")
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _listeningState.value = ListeningState.LISTENING
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                _rmsLevel.value = (rmsdB + 2f).coerceIn(0f, 10f) / 10f
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                _listeningState.value = ListeningState.PROCESSING
            }
            override fun onError(error: Int) {
                _listeningState.value = ListeningState.ERROR
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                    else -> "Speech recognition error"
                }
                onError(msg)
                _listeningState.value = ListeningState.IDLE
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                _listeningState.value = ListeningState.IDLE
                _rmsLevel.value = 0f
                if (text.isNotBlank()) onResult(text)
                else onError("Couldn't understand that.")
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _listeningState.value = ListeningState.IDLE
        _rmsLevel.value = 0f
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
