package com.foxtask.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foxtask.app.presentation.ui.theme.OrangePrimary
import kotlinx.coroutines.delay

@Composable
fun LevelUpAnimation(
    show: Boolean,
    newLevel: Int,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {}
) {
    var animatedScale by remember { mutableFloatStateOf(0f) }
    var animatedAlpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(show) {
        if (show) {
            animatedScale = 1.2f
            animatedAlpha = 1f
            // Анимация пульсации
            androidx.compose.animation.core.animate(
                initialValue = 1.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                )
            ) { value, _ ->
                animatedScale = value
            }
            delay(2500)
            animatedScale = 0f
            animatedAlpha = 0f
            onAnimationComplete()
        }
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn() + scaleIn(initialScale = 0f),
        exit = fadeOut() + scaleOut(targetScale = 0f)
    ) {
        Box(
            modifier = modifier
                .size(150.dp)
                .scale(scale = animatedScale),
            contentAlignment = Alignment.Center
        ) {
            // Контур или сияние вокруг лиса
            // Здесь можно добавить Lottie или частицы

            Text(
                text = "Уровень $newLevel!",
                color = OrangePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
