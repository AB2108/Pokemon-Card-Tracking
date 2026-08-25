package com.pokemontracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A single point on a [LineChart]: an x value (e.g. a date) and a y value. */
data class ChartPoint(val x: Float, val y: Float)

/**
 * A lightweight, dependency-free line chart drawn with Compose [Canvas].
 * Renders a filled area under the line, the data points, three horizontal grid
 * lines with y-axis labels and the first/last x-axis labels.
 */
@Composable
fun LineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    yLabel: (Float) -> String = { it.toString() },
    xLabel: (Float) -> String = { it.toString() },
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier.height(height),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Noch keine Daten für einen Verlauf",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val axisColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fillTop = lineColor.copy(alpha = 0.28f)
    val fillBottom = lineColor.copy(alpha = 0.02f)
    val density = LocalDensity.current
    val labelPx = with(density) { 11.dp.toPx() }

    Canvas(modifier = modifier.height(height)) {
        val leftPad = 96f
        val rightPad = 24f
        val topPad = 20f
        val bottomPad = 44f

        val chartLeft = leftPad
        val chartRight = size.width - rightPad
        val chartTop = topPad
        val chartBottom = size.height - bottomPad
        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)

        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val rawMinY = points.minOf { it.y }
        val rawMaxY = points.maxOf { it.y }
        // Pad the y-range a little and guard against a flat/degenerate series.
        val span = (rawMaxY - rawMinY)
        val minY = if (span == 0f) (rawMinY - 1f).coerceAtLeast(0f) else rawMinY - span * 0.1f
        val maxY = if (span == 0f) rawMaxY + 1f else rawMaxY + span * 0.1f
        val xRange = (maxX - minX).takeIf { it != 0f } ?: 1f
        val yRange = (maxY - minY).takeIf { it != 0f } ?: 1f

        fun xPix(x: Float) = chartLeft + (x - minX) / xRange * chartWidth
        fun yPix(y: Float) = chartBottom - (y - minY) / yRange * chartHeight

        // Horizontal grid lines + y labels at bottom / middle / top.
        val gridValues = listOf(minY, (minY + maxY) / 2f, maxY)
        val dashed = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
        gridValues.forEach { value ->
            val y = yPix(value)
            drawLine(
                color = axisColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1f,
                pathEffect = dashed,
            )
            drawContext.canvas.nativeCanvas.drawText(
                yLabel(value),
                8f,
                y + labelPx / 3f,
                android.graphics.Paint().apply {
                    color = labelColor.toArgb()
                    textSize = labelPx
                    isAntiAlias = true
                },
            )
        }

        val ordered = points.sortedBy { it.x }
        val pts = ordered.map { Offset(xPix(it.x), yPix(it.y)) }

        // Filled area under the line.
        if (pts.size >= 2) {
            val area = Path().apply {
                moveTo(pts.first().x, chartBottom)
                pts.forEach { lineTo(it.x, it.y) }
                lineTo(pts.last().x, chartBottom)
                close()
            }
            drawPath(
                path = area,
                brush = Brush.verticalGradient(
                    colors = listOf(fillTop, fillBottom),
                    startY = chartTop,
                    endY = chartBottom,
                ),
            )
        }

        // The line itself.
        if (pts.size >= 2) {
            val line = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
            }
            drawPath(
                path = line,
                color = lineColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round),
            )
        }

        // Data points.
        pts.forEach { p ->
            drawCircle(color = lineColor, radius = 5f, center = p)
        }

        // X labels for first and last points.
        drawXLabel(xLabel(ordered.first().x), xPix(ordered.first().x), size.height - 12f, labelColor.toArgb(), labelPx, alignStart = true)
        if (ordered.size > 1) {
            drawXLabel(xLabel(ordered.last().x), xPix(ordered.last().x), size.height - 12f, labelColor.toArgb(), labelPx, alignStart = false)
        }
    }
}

private fun DrawScope.drawXLabel(
    text: String,
    centerX: Float,
    y: Float,
    colorArgb: Int,
    textSizePx: Float,
    alignStart: Boolean,
) {
    val paint = android.graphics.Paint().apply {
        color = colorArgb
        textSize = textSizePx
        isAntiAlias = true
        textAlign = if (alignStart) android.graphics.Paint.Align.LEFT else android.graphics.Paint.Align.RIGHT
    }
    drawContext.canvas.nativeCanvas.drawText(text, centerX, y, paint)
}
