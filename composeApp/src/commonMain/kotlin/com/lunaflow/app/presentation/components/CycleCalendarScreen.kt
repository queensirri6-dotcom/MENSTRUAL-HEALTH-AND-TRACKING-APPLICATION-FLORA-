package com.lunaflow.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunaflow.app.data.local.entity.CycleRecord
import com.lunaflow.app.domain.manager.CycleData
import com.lunaflow.app.domain.manager.CycleManager
import com.lunaflow.app.domain.model.CyclePhase
import kotlinx.datetime.*

@Composable
fun CycleCalendarScreen(
    cycleData: CycleData,
    calendarYear: Int,
    calendarMonth: Int,
    recordsForMonth: List<CycleRecord>,
    predictedPeriods: List<LocalDate>,
    cycleLengthHistory: List<Int>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onLogPeriodStart: () -> Unit,
    onLogPeriodEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cycleManager = remember { CycleManager() }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val recordMap = remember(recordsForMonth) {
        recordsForMonth.associateBy { it.date }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ──────────────────────────────
        CalendarHeader(
            year = calendarYear,
            month = calendarMonth,
            onPrev = onPrevMonth,
            onNext = onNextMonth
        )

        // ── Phase Legend ────────────────────────
        PhaseLegend()

        // ── Day-of-week labels ───────────────────
        val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            dayNames.forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // ── Calendar Grid ───────────────────────
        val firstDay = LocalDate(calendarYear, calendarMonth, 1)
        val daysInMonth = when (firstDay.month) {
            Month.FEBRUARY -> if (isLeapYear(calendarYear)) 29 else 28
            Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
            else -> 31
        }
        val startOffset = firstDay.dayOfWeek.isoDayNumber % 7 // Sunday=0

        val cells = buildList {
            repeat(startOffset) { add(null) }
            for (d in 1..daysInMonth) add(LocalDate(calendarYear, calendarMonth, d))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(cells) { date ->
                if (date == null) {
                    Spacer(Modifier.size(40.dp))
                } else {
                    val phase = cycleManager.getPhaseForDate(date, cycleData)
                    val record = recordMap[date]
                    val isToday = date == today
                    val isFertility = cycleManager.isInFertilityWindow(date, cycleData)
                    val isOvulation = cycleManager.isOvulationDay(date, cycleData)
                    val isPredicted = date in predictedPeriods

                    CalendarDayCell(
                        date = date,
                        phase = phase,
                        isToday = isToday,
                        isFertilityWindow = isFertility,
                        isOvulation = isOvulation,
                        isPredictedPeriod = isPredicted,
                        hasLog = record != null,
                        isPeriodStart = record?.isPeriodStart == true,
                        moodScore = record?.moodScore ?: -1
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Period Start / End Buttons (F2) ─────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onLogPeriodStart,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
            ) {
                Text("🩸 Period Started", fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = onLogPeriodEnd,
                modifier = Modifier.weight(1f)
            ) {
                Text("✓ Period Ended", fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Predicted Periods (F4) ───────────────
        if (predictedPeriods.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "📅 Upcoming Periods",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    predictedPeriods.forEachIndexed { i, date ->
                        Text(
                            "• Cycle ${i + 1}: $date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // ── Cycle Length Trend (F3) ──────────────
        if (cycleLengthHistory.size >= 2) {
            Spacer(Modifier.height(16.dp))
            CycleLengthTrendChart(
                lengths = cycleLengthHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CalendarHeader(year: Int, month: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) { Text("◀", fontSize = 20.sp) }
        Text(
            "${monthNames[month - 1]} $year",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onNext) { Text("▶", fontSize = 20.sp) }
    }
}

@Composable
private fun PhaseLegend() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CyclePhase.values().forEach { phase ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(phase.phaseColor().copy(alpha = 0.8f))
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    phase.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    phase: CyclePhase,
    isToday: Boolean,
    isFertilityWindow: Boolean,
    isOvulation: Boolean,
    isPredictedPeriod: Boolean,
    hasLog: Boolean,
    isPeriodStart: Boolean,
    moodScore: Int
) {
    val phaseColor = phase.phaseColor()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                when {
                    isOvulation     -> Color(0xFFBA68C8).copy(alpha = 0.85f)
                    isFertilityWindow -> Color(0xFFBA68C8).copy(alpha = 0.3f)
                    isPredictedPeriod -> Color(0xFFE57373).copy(alpha = 0.4f)
                    else              -> phaseColor.copy(alpha = 0.25f)
                }
            )
            .then(
                if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp
            )
            // Indicator row
            Row {
                if (hasLog) Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF26C6DA)))
                if (isOvulation) Text("✦", fontSize = 6.sp, color = Color.White)
                if (isPeriodStart) Text("●", fontSize = 5.sp, color = Color(0xFFE57373))
            }
        }
    }
}

@Composable
fun CycleLengthTrendChart(lengths: List<Int>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📈 Cycle Length Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Last ${lengths.size} cycles",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            val minLen = (lengths.min() - 2).coerceAtLeast(14)
            val maxLen = (lengths.max() + 2).coerceAtMost(50)
            val range = (maxLen - minLen).toFloat().coerceAtLeast(1f)

            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                lengths.forEachIndexed { index, length ->
                    val fraction = (length - minLen) / range
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "$length",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height((fraction * 60f + 8f).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                        )
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
