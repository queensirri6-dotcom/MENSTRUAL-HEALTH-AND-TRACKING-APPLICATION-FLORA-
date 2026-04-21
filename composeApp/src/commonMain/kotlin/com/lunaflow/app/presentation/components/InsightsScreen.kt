package com.lunaflow.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.lunaflow.app.domain.model.CyclePhase
import kotlinx.datetime.LocalDate

@Composable
fun InsightsScreen(
    symptomFrequency: Map<String, Int>,
    phaseSymptomCorrelation: Map<CyclePhase, List<String>>,
    moodHistory: List<Pair<LocalDate, Int>>,
    cycleLengthHistory: List<Int>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "✨ Your Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Patterns discovered from your tracking data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // I1 — Symptom Frequency Chart
        if (symptomFrequency.isNotEmpty()) {
            SymptomFrequencyChart(
                data = symptomFrequency,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        } else {
            EmptyInsightCard(
                icon = "📊",
                title = "Symptom Trends",
                message = "Log symptoms over a few days to see your patterns here.",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        // I2 — Phase-Symptom Correlation
        if (phaseSymptomCorrelation.values.any { it.isNotEmpty() }) {
            PhaseSymptomCorrelationCard(
                data = phaseSymptomCorrelation,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        } else {
            EmptyInsightCard(
                icon = "🔗",
                title = "Phase Correlations",
                message = "Track symptoms across your full cycle to reveal which phase affects you most.",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        // I3 — Cycle Length History
        if (cycleLengthHistory.size >= 2) {
            CycleLengthTrendChart(
                lengths = cycleLengthHistory,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        } else {
            EmptyInsightCard(
                icon = "📅",
                title = "Cycle Length History",
                message = "Log 2+ periods to track how regular your cycle is.",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        // I4 — Mood Pattern
        if (moodHistory.size >= 3) {
            MoodPatternChart(
                moodHistory = moodHistory,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            EmptyInsightCard(
                icon = "🌈",
                title = "Mood Patterns",
                message = "Log your mood for 3+ days to see emotional patterns here.",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ── I1: Symptom Frequency Horizontal Bar Chart ──────────────────────

@Composable
fun SymptomFrequencyChart(data: Map<String, Int>, modifier: Modifier = Modifier) {
    val maxCount = data.values.maxOrNull() ?: 1
    val phaseColors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFFBA68C8),
        Color(0xFF81C784), Color(0xFFFFCC80), Color(0xFF4DD0E1)
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📊 Symptom Frequency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "How often each symptom was logged",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            data.entries.toList().sortedByDescending { it.value }.forEachIndexed { index, (symptom, count) ->
                val barFraction = count.toFloat() / maxCount.toFloat()
                val barColor = phaseColors[index % phaseColors.size]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                ) {
                    Text(
                        symptom.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.width(90.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = barFraction.coerceIn(0.05f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor.copy(alpha = 0.85f))
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${count}x",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── I2: Phase-to-Symptom Heatmap ────────────────────────────────────

@Composable
fun PhaseSymptomCorrelationCard(
    data: Map<CyclePhase, List<String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🔗 Phase-Symptom Patterns",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "What you experience in each phase",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            CyclePhase.values().forEach { phase ->
                val symptoms = data[phase] ?: emptyList()
                if (symptoms.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(phase.phaseColor().copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${phase.phaseEmoji()} ${phase.displayName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = phase.phaseColor(),
                                fontSize = 10.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            symptoms.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
}

// ── I4: Mood Pattern Bar Chart ──────────────────────────────────────

@Composable
fun MoodPatternChart(
    moodHistory: List<Pair<LocalDate, Int>>,
    modifier: Modifier = Modifier
) {
    val moodColors = listOf(
        Color(0xFF90A4AE), Color(0xFF80CBC4), Color(0xFFFFF176),
        Color(0xFFA5D6A7), Color(0xFFFFCC80)
    )
    val moodEmojis = listOf("😢", "😕", "😐", "😊", "😄")

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🌈 Mood History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Your mood across ${moodHistory.size} logged days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            val recentMoods = moodHistory.takeLast(14)
            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                recentMoods.forEach { (date, score) ->
                    val fraction = (score + 1) / 5f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(moodEmojis[score], fontSize = 10.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((fraction * 50f + 8f).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(moodColors[score].copy(alpha = 0.85f))
                        )
                        Text(
                            date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            // Average mood
            val avg = moodHistory.map { it.second }.average()
            val avgEmoji = moodEmojis[(avg.coerceIn(0.0, 4.0).toInt())]
            Text(
                "Average mood: $avgEmoji (${"%.1f".format(avg)}/4)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Empty State Card ────────────────────────────────────────────────

@Composable
fun EmptyInsightCard(icon: String, title: String, message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
