package com.lunaflow.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WaterIntakeCard(
    cups: Int, // 0..8
    onAddCup: () -> Unit,
    onRemoveCup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxCups = 8
    val filledColor = Color(0xFF29B6F6)   // sky blue
    val emptyColor  = Color(0xFFE0F7FA)

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "💧 Water Intake",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "$cups / $maxCups cups",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until maxCups) {
                    val isFilled = i < cups
                    WaterCupIcon(
                        isFilled = isFilled,
                        filledColor = filledColor,
                        emptyColor = emptyColor,
                        onClick = {
                            if (isFilled) onRemoveCup()
                            else onAddCup()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { cups / maxCups.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = filledColor,
                trackColor = emptyColor
            )

            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    cups == 0 -> "Tap a cup to log your water intake!"
                    cups < 4  -> "Keep going! Staying hydrated helps with cramps. 💙"
                    cups < 7  -> "Great progress! You're doing well! 🌊"
                    else      -> "Amazing! You've hit your hydration goal! 🎉"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WaterCupIcon(
    isFilled: Boolean,
    filledColor: Color,
    emptyColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isFilled) filledColor.copy(alpha = 0.85f) else emptyColor)
            .border(1.dp, if (isFilled) filledColor else Color(0xFFB0BEC5), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .scale(scale)
    ) {
        Text(
            if (isFilled) "💧" else "○",
            fontSize = if (isFilled) 18.sp else 14.sp,
            color = if (isFilled) Color.White else Color(0xFFB0BEC5)
        )
    }
}
