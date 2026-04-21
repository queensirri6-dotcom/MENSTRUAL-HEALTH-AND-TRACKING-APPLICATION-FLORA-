package com.lunaflow.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FlowIntensityCard(
    selectedLevel: Int,  // 0=none, 1=spotting, 2=light, 3=medium, 4=heavy
    currentPhaseIsMenstrual: Boolean,
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!currentPhaseIsMenstrual && selectedLevel == 0) return // Hide if not menstrual and nothing logged

    val levels = listOf(
        FlowLevel(0, "None",     Color(0xFFE0E0E0), "○"),
        FlowLevel(1, "Spotting", Color(0xFFFFCDD2), "🩸"),
        FlowLevel(2, "Light",    Color(0xFFEF9A9A), "🩸🩸"),
        FlowLevel(3, "Medium",   Color(0xFFE57373), "🩸🩸🩸"),
        FlowLevel(4, "Heavy",    Color(0xFFC62828), "🩸🩸🩸🩸")
    )

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🩸 Flow Intensity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                levels.forEach { level ->
                    FlowLevelChip(
                        level = level,
                        isSelected = selectedLevel == level.value,
                        onClick = { onLevelSelected(level.value) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FlowLevelChip(
    level: FlowLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.3f,
        animationSpec = tween(200)
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(level.color.copy(alpha = animatedAlpha))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = level.color,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (level.value == 0) level.icon else "🩸",
                fontSize = 16.sp
            )
            Text(
                text = level.label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class FlowLevel(
    val value: Int,
    val label: String,
    val color: Color,
    val icon: String
)
