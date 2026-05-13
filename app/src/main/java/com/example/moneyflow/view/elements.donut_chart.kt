package com.example.moneyflow.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneyflow.model.TransactionSummary
import com.example.moneyflow.ui.theme.BackgroundDialog
import com.example.moneyflow.ui.theme.TextPrimary

data class DonutSlice(val color: Color, val percentage: Float)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 36.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset  = stroke / 2f
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)

            if (slices.isEmpty()) {
                drawArc(
                    color = BackgroundDialog.copy(alpha = 0.4f),
                    startAngle = 0f, sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(width = stroke)
                )
            } else {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweep = 360f * (slice.percentage / 100f)
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle, sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft, size = arcSize,
                        style = Stroke(width = stroke)
                    )
                    startAngle += sweep
                }
            }
        }

        Text(
            text       = centerLabel,
            color      = TextPrimary,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun transactionSlices(summaries: List<TransactionSummary>): List<DonutSlice> =
    summaries.map { DonutSlice(Color(it.transaction.categoryColor), it.percentage) }