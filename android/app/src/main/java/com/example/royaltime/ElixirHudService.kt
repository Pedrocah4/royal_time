package com.example.royaltime

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

class ElixirHudService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null

    private var txtElixir: TextView? = null
    private var txtStatus: TextView? = null
    private var txtHistory: TextView? = null
    private var btnClose: View? = null

    private var voiceInputManager: VoiceInputManager? = null

    // Lógica de Elixir
    private var elixirCurrent = 5
    private var multiplierText = "1x"
    private var multiplierVal = 1.0f
    private var isGameStarted = false
    
    private val BASE_INTERVAL = 2800L // 2.8s por elixir no 1x
    private var currentInterval = BASE_INTERVAL
    private var lastElixirTime = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val elixirRunnable = object : Runnable {
        override fun run() {
            if (isGameStarted && elixirCurrent < 10) {
                val now = System.currentTimeMillis()
                if (now - lastElixirTime >= currentInterval) {
                    elixirCurrent++
                    lastElixirTime = now
                    updateUi()
                }
            }
            handler.postDelayed(this, 100)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // 1. Inflar a View do HUD
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_hud, null)

        // 2. Definir Parâmetros da Janela de Sobreposição
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        // 3. Adicionar a View ao WindowManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.addView(floatingView, params)

        // 4. Referenciar elementos do layout
        txtElixir = floatingView?.findViewById(R.id.txt_elixir)
        txtStatus = floatingView?.findViewById(R.id.txt_status)
        txtHistory = floatingView?.findViewById(R.id.txt_history)
        btnClose = floatingView?.findViewById(R.id.btn_close)

        // 5. Configurar arraste (drag)
        setupDragTouch()

        // 6. Configurar clique de fechar
        btnClose?.setOnClickListener {
            stopSelf()
        }

        // 7. Configurar reconhecimento de voz
        setupVoiceInput()

        // 8. Iniciar o Loop de Atualização do Elixir
        handler.post(elixirRunnable)
    }

    private fun setupDragTouch() {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null || params == null) return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params!!.x
                        initialY = params!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                        params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun setupVoiceInput() {
        voiceInputManager = VoiceInputManager(this) { command ->
            handler.post {
                handleVoiceCommand(command)
            }
        }
        voiceInputManager?.startListening()
    }

    private fun handleVoiceCommand(command: VoiceCommand) {
        when (command) {
            is VoiceCommand.Iniciar -> {
                isGameStarted = true
                elixirCurrent = 5
                lastElixirTime = System.currentTimeMillis()
                txtStatus?.text = "1x | Em Partida"
                txtHistory?.text = "🎮 Partida Iniciada!"
                updateUi()
            }
            is VoiceCommand.AlterarMultiplicador -> {
                if (!isGameStarted) return
                multiplierText = command.texto
                multiplierVal = command.multiplicador
                currentInterval = (BASE_INTERVAL / multiplierVal).toLong()
                lastElixirTime = System.currentTimeMillis()
                txtStatus?.text = "$multiplierText | Em Partida"
                txtHistory?.text = "⚡ Velocidade: $multiplierText"
                updateUi()
            }
            is VoiceCommand.GastarElixir -> {
                if (!isGameStarted) return
                val cost = command.quantidade
                val detail = command.detalhe
                if (elixirCurrent >= cost) {
                    elixirCurrent -= cost
                    txtHistory?.text = "🎙️ $detail (-$cost)"
                } else {
                    txtHistory?.text = "⚠️ Sem elixir p/ $detail (-$cost)"
                }
                updateUi()
            }
        }
    }

    private fun updateUi() {
        txtElixir?.text = "💧 $elixirCurrent"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(elixirRunnable)
        voiceInputManager?.stopListening()
        voiceInputManager?.destroy()
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
        }
        Toast.makeText(this, "HUD Encerrado", Toast.LENGTH_SHORT).show()
    }
}
