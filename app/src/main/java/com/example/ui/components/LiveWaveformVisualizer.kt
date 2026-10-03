package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberLED
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCyan
import kotlin.math.abs

@Composable
fun LiveWaveformVisualizer(
    buffer: FloatArray,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(CyberDarkBg, RoundedCornerShape(8.dp))
            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            // Draw Background Grid Lines
            val gridStepX = width / 12f
            for (i in 1..11) {
                drawLine(
                    color = CyberBorder.copy(alpha = 0.4f),
                    start = Offset(i * gridStepX, 0f),
                    end = Offset(i * gridStepX, height),
                    strokeWidth = 1f
                )
            }
            drawLine(
                color = CyberBorder.copy(alpha = 0.6f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 1f
            )

            if (buffer.isEmpty()) return@Canvas

            // Draw Frequency Spectrum Bars (Background Layer)
            val barCount = 32
            val barWidth = width / barCount
            val step = maxOf(1, buffer.size / barCount)

            for (b in 0 until barCount) {
                val sampleIdx = (b * step).coerceIn(0, buffer.size - 1)
                val amp = abs(buffer[sampleIdx]).coerceIn(0f, 1f)
                val barHeight = (amp * (height * 0.8f)).coerceAtLeast(4f)

                val x = b * barWidth + barWidth * 0.1f
                val y = height - barHeight

                val color = when {
                    b < 10 -> MatrixGreen.copy(alpha = 0.4f)
                    b < 22 -> NeonCyan.copy(alpha = 0.4f)
                    else -> ElectricMagenta.copy(alpha = 0.4f)
                }

                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, barHeight)
                )
            }

            // Draw Real-Time Oscilloscope Waveform Line (Foreground Layer)
            val path = Path()
            val pointsCount = buffer.size
            val stepX = width / (pointsCount - 1).toFloat()

            for (i in 0 until pointsCount) {
                val sample = buffer[i].coerceIn(-1.0f, 1.0f)
                val x = i * stepX
                val y = centerY - (sample * (centerY * 0.85f))

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonCyan, MatrixGreen, ElectricMagenta, AmberLED)
                ),
                style = Stroke(width = 2.5f)
            )
        }
    }
}
