package com.example.royaltime

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

sealed class VoiceCommand {
    object Iniciar : VoiceCommand()
    class AlterarMultiplicador(val multiplicador: Float, val texto: String) : VoiceCommand()
    class GastarElixir(val quantidade: Int, val detalhe: String) : VoiceCommand()
}

class VoiceInputManager(
    private val context: Context,
    private val onCommandReceived: (VoiceCommand) -> Unit
) : RecognitionListener {

    private val TAG = "VoiceInputManager"
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var isListening = false

    private val cardCosts = mapOf(
        "gigante" to 5, "golem" to 8, "tronco" to 2, "corredor" to 4,
        "balao" to 5, "balão" to 5, "mago" to 5, "valquiria" to 4, "valquíria" to 4,
        "pekka" to 7, "peka" to 7, "mini pekka" to 4, "mini peka" to 4,
        "mineiro" to 3, "principe" to 5, "príncipe" to 5, "bruxa" to 5,
        "bebe dragao" to 4, "bebê dragão" to 4, "horda" to 5, "exercito" to 3,
        "exército" to 3, "barril" to 3, "megacavaleiro" to 7, "mega cavaleiro" to 7,
        "sparky" to 6, "foguete" to 6, "bola de fogo" to 4, "veneno" to 4,
        "relampago" to 6, "relâmpago" to 6, "servos" to 3, "arqueiras" to 3,
        "cavaleiro" to 3, "mosqueteira" to 4, "espirito" to 1, "espírito" to 1,
        "goblins" to 2, "gangue" to 3, "lapide" to 3, "lápide" to 3,
        "tres mosqueteiras" to 9, "três mosqueteiras" to 9,
        "pequeno principe" to 3, "pequeno príncipe" to 3,
        "barbaros" to 5, "bárbaros" to 5, "barbaros de elite" to 6, "bárbaros de elite" to 6,
        "dragao infernal" to 4, "dragão infernal" to 4, "torre inferno" to 5,
        "x-besta" to 6, "xbesta" to 6, "morteiro" to 4, "arqueiro magico" to 4,
        "arqueiro mágico" to 4, "bandida" to 3, "fantasma" to 3, "lenhador" to 4,
        "bruxa sombria" to 4, "cemiterio" to 5, "cemitério" to 5, "lava" to 7,
        "ariete" to 4, "aríete" to 4, "fenix" to 4, "fênix" to 4
    )

    private val numberCosts = mapOf(
        "um" to 1, "1" to 1,
        "dois" to 2, "2" to 2,
        "tres" to 3, "três" to 3, "3" to 3,
        "quatro" to 4, "4" to 4,
        "cinco" to 5, "5" to 5,
        "seis" to 6, "6" to 6,
        "sete" to 7, "7" to 7,
        "oito" to 8, "8" to 8,
        "nove" to 9, "9" to 9,
        "dez" to 10, "10" to 10
    )

    init {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(this)

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "pt-BR")
        }
    }

    fun startListening() {
        if (!isListening) {
            isListening = true
            speechRecognizer?.startListening(recognizerIntent)
            Log.d(TAG, "Speech Recognition Started")
        }
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        Log.d(TAG, "Speech Recognition Stopped")
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun processText(text: String) {
        val cleanText = text.lowercase(Locale.ROOT).trim()
        Log.d(TAG, "Processing voice input: $cleanText")

        // 1. Verificar comandos de início/reinício
        if (cleanText.contains("iniciar") || cleanText.contains("começar") || cleanText.contains("start")) {
            onCommandReceived(VoiceCommand.Iniciar)
            return
        }

        // 2. Verificar multiplicadores de elixir
        if (cleanText.contains("vezes dois") || cleanText.contains("dobro") || cleanText.contains("duas vezes")) {
            onCommandReceived(VoiceCommand.AlterarMultiplicador(2.0f, "2x"))
            return
        }
        if (cleanText.contains("vezes tres") || cleanText.contains("vezes três") || cleanText.contains("triplo")) {
            onCommandReceived(VoiceCommand.AlterarMultiplicador(3.0f, "3x"))
            return
        }
        if (cleanText.contains("vezes um") || cleanText.contains("normal") || cleanText.contains("uma vez")) {
            onCommandReceived(VoiceCommand.AlterarMultiplicador(1.0f, "1x"))
            return
        }

        // 3. Verificar cartas conhecidas na fala
        for ((card, cost) in cardCosts) {
            if (cleanText.contains(card)) {
                val formattedName = card.substring(0, 1).uppercase(Locale.ROOT) + card.substring(1)
                onCommandReceived(VoiceCommand.GastarElixir(cost, formattedName))
                return
            }
        }

        // 4. Verificar números falados
        for ((numberWord, cost) in numberCosts) {
            val words = cleanText.split("\\s+".toRegex())
            if (words.contains(numberWord)) {
                onCommandReceived(VoiceCommand.GastarElixir(cost, "$cost Elixir"))
                return
            }
        }
    }

    // --- RecognitionListener overrides ---

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech")
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        Log.d(TAG, "End of speech")
    }

    override fun onError(error: Int) {
        Log.e(TAG, "Speech Recognizer Error: $error")
        if (isListening) {
            // Reinicia a escuta se foi interrompido por timeout/no-match
            speechRecognizer?.startListening(recognizerIntent)
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0]
            processText(spokenText)
        }

        if (isListening) {
            speechRecognizer?.startListening(recognizerIntent)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
