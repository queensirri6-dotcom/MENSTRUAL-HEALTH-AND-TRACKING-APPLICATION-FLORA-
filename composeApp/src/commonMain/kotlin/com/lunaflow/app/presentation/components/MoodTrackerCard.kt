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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunaflow.app.domain.model.CyclePhase

@Composable
fun MoodTrackerCard(
    selectedMoodScore: Int, // -1 = none logged
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val moods = listOf(
        MoodEntry(0, "😢", "Sad",     Color(0xFF90A4AE)),
        MoodEntry(1, "😕", "Low",     Color(0xFF80CBC4)),
        MoodEntry(2, "😐", "Neutral", Color(0xFFFFF176)),
        MoodEntry(3, "😊", "Good",    Color(0xFFA5D6A7)),
        MoodEntry(4, "😄", "Great",   Color(0xFFFFCC80))
    )

    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🌸 How are you feeling today?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                moods.forEach { mood ->
                    MoodItem(
                        mood = mood,
                        isSelected = selectedMoodScore == mood.score,
                        onClick = { onMoodSelected(mood.score) }
                    )
                }
            }

            if (selectedMoodScore >= 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Logged: ${moods[selectedMoodScore].label} ${moods[selectedMoodScore].emoji}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun MoodItem(mood: MoodEntry, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
            .scale(scale)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) mood.color.copy(alpha = 0.9f)
                    else mood.color.copy(alpha = 0.25f)
                )
                .then(
                    if (isSelected) Modifier.border(2.dp, mood.color, CircleShape)
                    else Modifier
                )
        ) {
            Text(mood.emoji, fontSize = 26.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            mood.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class MoodEntry(
    val score: Int,
    val emoji: String,
    val label: String,
    val color: Color
)
