package com.example.royaltime

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class ElixirCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var elixirCurrent: Int = 5
        set(value) {
            field = value
            invalidate()
        }

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    // Cores e Configurações Visuais
    private val bezelColor = Color.parseColor("#D5D8DC") // Prateado claro
    private val bezelDarkColor = Color.parseColor("#7B7D7D") // Prateado escuro/borda interna
    private val trackColor = Color.parseColor("#2614002C") // Pista roxa escura semitransparente (15%)
    private val progressColor = Color.parseColor("#FF8A2BE2") // Roxo de elixir vibrante

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val glossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.argb(130, 255, 255, 255)
        strokeCap = Paint.Cap.ROUND
    }

    // Objetos cacheados para evitar alocações em tempo de renderização
    private val oval = RectF()
    private val glossOval = RectF()
    private val textBounds = Rect()
    private var glassShader: Shader? = null
    private var textShader: Shader? = null

    // Variáveis de Animação
    private var pulseScale = 1f

    fun triggerPulse() {
        pulseScale = 1.15f
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = min(w, h).toFloat()
        val glowPadding = size * 0.08f
        val ringRadius = (size / 2f) - glowPadding
        val progressStrokeWidth = size * 0.09f
        val cx = w / 2f
        val cy = h / 2f

        oval.set(
            cx - ringRadius,
            cy - ringRadius,
            cx + ringRadius,
            cy + ringRadius
        )

        glossPaint.strokeWidth = size * 0.022f

        val innerRadius = ringRadius - progressStrokeWidth / 2f - size * 0.015f
        if (innerRadius > 0) {
            glossOval.set(
                cx - innerRadius * 0.8f,
                cy - innerRadius * 0.8f,
                cx + innerRadius * 0.8f,
                cy + innerRadius * 0.8f
            )

            glassShader = RadialGradient(
                cx - innerRadius * 0.3f,
                cy - innerRadius * 0.3f,
                innerRadius * 1.4f,
                intArrayOf(Color.WHITE, Color.parseColor("#F2F3F4"), Color.parseColor("#BDC3C7"), Color.parseColor("#95A5A6")),
                floatArrayOf(0f, 0.3f, 0.8f, 1f),
                Shader.TileMode.CLAMP
            )

            val textSize = innerRadius * 1.1f
            textPaint.textSize = textSize

            textShader = LinearGradient(
                cx, cy - textSize * 0.5f,
                cx, cy + textSize * 0.3f,
                intArrayOf(
                    Color.parseColor("#FFF9C4"),
                    Color.parseColor("#F5B041"),
                    Color.parseColor("#D35400")
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val size = min(width, height).toFloat()

        val glowPadding = size * 0.08f
        val ringRadius = (size / 2f) - glowPadding
        val progressStrokeWidth = size * 0.09f

        // Salvar e aplicar escala de pulso se houver animação ativa
        val hasPulse = pulseScale > 1f
        if (hasPulse) {
            canvas.save()
            canvas.scale(pulseScale, pulseScale, cx, cy)
        }

        // 1. Outer Glow (Brilho Externo Roxo Translúcido)
        ringPaint.style = Paint.Style.STROKE
        for (i in 0..6) {
            ringPaint.color = Color.argb((18 - i * 2.5).toInt().coerceAtLeast(0), 138, 43, 226)
            ringPaint.strokeWidth = progressStrokeWidth + i * 4f
            canvas.drawCircle(cx, cy, ringRadius, ringPaint)
        }

        // 2. Pista de Progresso (Track de Fundo Escuro)
        ringPaint.color = trackColor
        ringPaint.strokeWidth = progressStrokeWidth
        canvas.drawCircle(cx, cy, ringRadius, ringPaint)

        // 3. Bezel Metálico Externo e Interno
        // Bezel Externo (Borda Prateada Externa)
        ringPaint.color = bezelColor
        ringPaint.strokeWidth = size * 0.012f
        canvas.drawCircle(cx, cy, ringRadius + progressStrokeWidth / 2f + ringPaint.strokeWidth / 2f, ringPaint)
        
        // Bezel Interno (Borda Prateada Interna)
        ringPaint.color = bezelDarkColor
        canvas.drawCircle(cx, cy, ringRadius - progressStrokeWidth / 2f - ringPaint.strokeWidth / 2f, ringPaint)

        // 4. Anel de Progresso Roxo (Arco de Progresso)
        if (progress > 0f) {
            ringPaint.color = progressColor
            ringPaint.strokeWidth = progressStrokeWidth
            ringPaint.strokeCap = Paint.Cap.ROUND
            
            // Desenha a partir de -90 graus (12 horas) no sentido horário
            canvas.drawArc(oval, -90f, progress * 360f, false, ringPaint)
        }

        // 5. Orbe de Vidro Interno (Esfera 3D Central)
        val innerRadius = ringRadius - progressStrokeWidth / 2f - size * 0.015f
        if (innerRadius > 0) {
            fillPaint.shader = glassShader
            canvas.drawCircle(cx, cy, innerRadius, fillPaint)
            fillPaint.shader = null

            // 6. Brilho Reflexivo (Gloss / Reflexo de luz diagonal superior esquerda)
            canvas.drawArc(glossOval, -165f, 75f, false, glossPaint)

            // 7. Número de Elixir Central 3D (Gradiente Dourado Vertical + Drop Shadow)
            textPaint.shader = textShader
            
            // Sombra projetada para efeito 3D
            textPaint.setShadowLayer(
                size * 0.025f, 
                size * 0.012f, 
                size * 0.018f, 
                Color.parseColor("#993E2723")
            )

            // Centraliza o texto verticalmente com precisão
            val textStr = elixirCurrent.toString()
            textPaint.getTextBounds(textStr, 0, textStr.length, textBounds)
            val textHeight = textBounds.height()
            
            canvas.drawText(textStr, cx, cy + textHeight / 2f, textPaint)
            
            textPaint.shader = null
            textPaint.clearShadowLayer()
        }

        // Restaurar estado e atualizar fator de escala
        if (hasPulse) {
            canvas.restore()
            pulseScale -= 0.015f
            if (pulseScale < 1f) {
                pulseScale = 1f
            }
            postInvalidateOnAnimation()
        }
    }
}
