package com.example.royaltime

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private var isListening = false
    private var preferOffline = true

    private val handler = Handler(Looper.getMainLooper())
    private val processedCommandsInSession = mutableSetOf<String>()
    private val detectedOpponentCards = mutableSetOf<String>()

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
    }

    private fun getRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "pt-BR")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOffline)
            }
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    fun startListening() {
        if (!isListening) {
            isListening = true
            speechRecognizer?.startListening(getRecognizerIntent())
            Log.d(TAG, "Speech Recognition Started (Prefer Offline: $preferOffline)")
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
        var tempText = cleanText

        // 1. Verificar comandos de início/reinício
        if (tempText.contains("iniciar") || tempText.contains("começar") || tempText.contains("start")) {
            val key = "cmd_iniciar"
            if (!processedCommandsInSession.contains(key)) {
                processedCommandsInSession.add(key)
                detectedOpponentCards.clear() // Reinicia o deck aprendido para a nova partida
                Log.d(TAG, "Novo jogo iniciado: deck do oponente resetado.")
                onCommandReceived(VoiceCommand.Iniciar)
            }
        }

        // 2. Verificar multiplicadores de elixir
        if (tempText.contains("vezes dois") || tempText.contains("dobro") || tempText.contains("duas vezes")) {
            val key = "cmd_mult_2x"
            if (!processedCommandsInSession.contains(key)) {
                processedCommandsInSession.add(key)
                onCommandReceived(VoiceCommand.AlterarMultiplicador(2.0f, "2x"))
            }
        } else if (tempText.contains("vezes tres") || tempText.contains("vezes três") || tempText.contains("triplo")) {
            val key = "cmd_mult_3x"
            if (!processedCommandsInSession.contains(key)) {
                processedCommandsInSession.add(key)
                onCommandReceived(VoiceCommand.AlterarMultiplicador(3.0f, "3x"))
            }
        } else if (tempText.contains("vezes um") || tempText.contains("normal") || tempText.contains("uma vez")) {
            val key = "cmd_mult_1x"
            if (!processedCommandsInSession.contains(key)) {
                processedCommandsInSession.add(key)
                onCommandReceived(VoiceCommand.AlterarMultiplicador(1.0f, "1x"))
            }
        }

        // 3. Verificar cartas conhecidas na fala (ordenadas por tamanho decrescente)
        val sortedCards = cardCosts.keys.sortedByDescending { it.length }
        for (card in sortedCards) {
            if (tempText.contains(card)) {
                // Se o deck já está completo (8 cartas), só aceita se a carta já pertence ao deck aprendido
                if (detectedOpponentCards.size >= 8 && !detectedOpponentCards.contains(card)) {
                    continue
                }

                if (!processedCommandsInSession.contains(card)) {
                    processedCommandsInSession.add(card)
                    
                    // Adiciona ao deck aprendido do adversário se houver espaço
                    if (detectedOpponentCards.size < 8 && !detectedOpponentCards.contains(card)) {
                        detectedOpponentCards.add(card)
                        Log.d(TAG, "Carta aprendida no deck do oponente: $card. Total: ${detectedOpponentCards.size}/8")
                    }

                    val cost = cardCosts[card] ?: 0
                    val formattedName = card.substring(0, 1).uppercase(Locale.ROOT) + card.substring(1)
                    onCommandReceived(VoiceCommand.GastarElixir(cost, formattedName))
                }
                tempText = tempText.replace(card, "")
            }
        }

        // 4. Verificar números falados
        val sortedNumbers = numberCosts.keys.sortedByDescending { it.length }
        for (numberWord in sortedNumbers) {
            val pattern = "\\b$numberWord\\b".toRegex()
            if (tempText.contains(pattern)) {
                if (!processedCommandsInSession.contains(numberWord)) {
                    processedCommandsInSession.add(numberWord)
                    val cost = numberCosts[numberWord] ?: 0
                    onCommandReceived(VoiceCommand.GastarElixir(cost, "$cost Elixir"))
                }
                tempText = tempText.replace(pattern, "")
            }
        }
    }

    // --- RecognitionListener overrides ---

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "Beginning of speech")
        processedCommandsInSession.clear()
    }

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        Log.d(TAG, "End of speech")
    }

    override fun onError(error: Int) {
        Log.e(TAG, "Speech Recognizer Error: $error")
        processedCommandsInSession.clear()

        // Se o erro for de linguagem offline indisponível (13) ou não suportada (12), desativa preferOffline
        if (error == 13 || error == 12) {
            Log.w(TAG, "Offline language pack not available. Falling back to online recognition.")
            preferOffline = false
        }

        if (isListening) {
            handler.postDelayed({
                if (isListening) {
                    speechRecognizer?.cancel()
                    speechRecognizer?.startListening(getRecognizerIntent())
                }
            }, 500)
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0]
            processText(spokenText)
        }

        processedCommandsInSession.clear()

        if (isListening) {
            speechRecognizer?.startListening(getRecognizerIntent())
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0]
            processText(spokenText)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
