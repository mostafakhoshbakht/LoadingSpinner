package com.component.loadingspinner

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        LoadingSpinner()
    }
}
@Composable
fun LoadingSpinner(
    size: Dp = 36.dp,
    color: Color = MaterialTheme.colors.primary,
    strokeWidth: Dp = size / 6,
    sweepTimeMillis: Int = 1300,
    rotationSpeedMultiplier: Float = 2f,
    minAngle: Float = 3f,
    maxAngle: Float = 270f,
    modifier: Modifier = Modifier.size(size),
) {
    //correct the parameters if they are not in the correct range
    val minAngle = remember { if (minAngle in 0f..360f) minAngle else 0f }
    val maxAngle = remember { if (maxAngle in 0f..360f && maxAngle >= minAngle) maxAngle else 360f }

    //Calculate Speeds
    val sweepSpeed = remember { (360f + maxAngle - minAngle) / (360f * sweepTimeMillis) }
    val rotationSpeed = remember { (360 + minAngle - maxAngle) / sweepTimeMillis }
    val extraRotationSpeed = remember { (rotationSpeedMultiplier - 1) * rotationSpeed }

    //phases times
    val phase1Duration =
        remember { (minAngle / (360f * sweepSpeed)).toInt() }
    val phase2Duration =
        remember { (phase1Duration + (maxAngle - minAngle) / (360f * sweepSpeed)).toInt() }
    val phase3Duration =
        remember { (phase2Duration + (360f - maxAngle) / (360f * sweepSpeed)).toInt() }
    val phase4Duration =
        remember { (phase3Duration + (maxAngle - minAngle) / (360f * sweepSpeed)).toInt() }

    val phase1StartAngle =
        remember { phase1Duration * rotationSpeed }
    val phase2StartAngle =
        remember { phase1StartAngle + (phase2Duration - phase1Duration) * rotationSpeed }
    val phase3StartAngle =
        remember { phase2StartAngle + (phase3Duration - phase2Duration) * rotationSpeed }
    val phase4StartAngle = { phase3StartAngle + (phase4Duration - phase3Duration) * rotationSpeed }
    val phase5Duration = remember { sweepTimeMillis + phase1Duration }
    val phase6Duration = remember { sweepTimeMillis + phase2Duration }
    val phase7Duration = remember { sweepTimeMillis + phase3Duration }
    val phase8Duration = remember { sweepTimeMillis + phase4Duration }

    val extraRotationDurationMillis = remember { (360f / extraRotationSpeed).toInt() }

    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")

    val startAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = sweepTimeMillis
                phase1StartAngle at phase1Duration using LinearEasing
                phase2StartAngle at phase2Duration using LinearEasing
                phase3StartAngle at phase3Duration using LinearEasing
                360f at phase4Duration using LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ), label = "startAngle"
    )
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = minAngle,
        targetValue = minAngle,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = sweepTimeMillis
                minAngle at phase1Duration using LinearEasing
                maxAngle at phase2Duration using LinearEasing
                maxAngle at phase3Duration using LinearEasing
                minAngle at phase4Duration using LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ), label = "sweepAngle"
    )
    val extraRotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                extraRotationDurationMillis,
                easing = LinearEasing
            )
        )
    )
    val density = LocalDensity.current
    val strokeWidthPixel = with(density) { remember { strokeWidth.toPx() } }
    val arcSizeWithStrokePixel = with(density) { remember { size.toPx() - strokeWidthPixel } }
    val arcCenter = with(density) { remember { Offset(size.toPx() / 2, size.toPx() / 2) } }
    val topLeft = remember {
        Offset(
            arcCenter.x - arcSizeWithStrokePixel / 2,
            arcCenter.y - arcSizeWithStrokePixel / 2
        )
    }

    Canvas(
        modifier = modifier
            .rotate(extraRotateAngle)

    ) {
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = Size(arcSizeWithStrokePixel, arcSizeWithStrokePixel),
            style = Stroke(width = strokeWidthPixel, cap = StrokeCap.Round)
        )
    }
}