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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val size = min(width, height).toFloat()

        // Espaçamento externo para o brilho
        val glowPadding = size * 0.08f
        val ringRadius = (size / 2f) - glowPadding
        val progressStrokeWidth = size * 0.09f // Espessura do anel de progresso

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
            
            val oval = RectF(
                cx - ringRadius,
                cy - ringRadius,
                cx + ringRadius,
                cy + ringRadius
            )
            
            // Desenha a partir de -90 graus (12 horas) no sentido horário
            canvas.drawArc(oval, -90f, progress * 360f, false, ringPaint)
        }

        // 5. Orbe de Vidro Interno (Esfera 3D Central)
        val innerRadius = ringRadius - progressStrokeWidth / 2f - size * 0.015f
        if (innerRadius > 0) {
            val glassShader = RadialGradient(
                cx - innerRadius * 0.3f,
                cy - innerRadius * 0.3f,
                innerRadius * 1.4f,
                intArrayOf(Color.WHITE, Color.parseColor("#F2F3F4"), Color.parseColor("#BDC3C7"), Color.parseColor("#95A5A6")),
                floatArrayOf(0f, 0.3f, 0.8f, 1f),
                Shader.TileMode.CLAMP
            )
            fillPaint.shader = glassShader
            canvas.drawCircle(cx, cy, innerRadius, fillPaint)
            fillPaint.shader = null

            // 6. Brilho Reflexivo (Gloss / Reflexo de luz diagonal superior esquerda)
            val glossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.argb(130, 255, 255, 255)
                strokeWidth = size * 0.022f
                strokeCap = Paint.Cap.ROUND
            }
            val glossOval = RectF(
                cx - innerRadius * 0.8f,
                cy - innerRadius * 0.8f,
                cx + innerRadius * 0.8f,
                cy + innerRadius * 0.8f
            )
            canvas.drawArc(glossOval, -165f, 75f, false, glossPaint)

            // 7. Número de Elixir Central 3D (Gradiente Dourado Vertical + Drop Shadow)
            val textSize = innerRadius * 1.1f
            textPaint.textSize = textSize
            
            val textShader = LinearGradient(
                cx, cy - textSize * 0.5f,
                cx, cy + textSize * 0.3f,
                intArrayOf(
                    Color.parseColor("#FFF9C4"), // Amarelo brilhante superior
                    Color.parseColor("#F5B041"), // Dourado médio
                    Color.parseColor("#D35400")  // Dourado escuro / Laranja
                ),
                null,
                Shader.TileMode.CLAMP
            )
            textPaint.shader = textShader
            
            // Sombra projetada para efeito 3D
            textPaint.setShadowLayer(
                size * 0.025f, 
                size * 0.012f, 
                size * 0.018f, 
                Color.parseColor("#993E2723") // Sombra marrom-escura de profundidade
            )

            // Centraliza o texto verticalmente com precisão
            val textBounds = Rect()
            val textStr = elixirCurrent.toString()
            textPaint.getTextBounds(textStr, 0, textStr.length, textBounds)
            val textHeight = textBounds.height()
            
            canvas.drawText(textStr, cx, cy + textHeight / 2f, textPaint)
            
            textPaint.shader = null
            textPaint.clearShadowLayer()
        }
    }
}
