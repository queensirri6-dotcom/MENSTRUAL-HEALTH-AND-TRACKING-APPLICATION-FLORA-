package com.lunaflow.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lunaflow.app.data.local.LunaDatabase
import com.lunaflow.app.data.local.getDatabaseBuilder
import com.lunaflow.app.data.local.getRoomDatabase
import com.lunaflow.app.data.remote.GeminiService
import com.lunaflow.app.data.repository.LunaRepositoryImpl
import com.lunaflow.app.data.repository.UserPreferencesRepository
import com.lunaflow.app.domain.manager.CycleManager
import com.lunaflow.app.domain.model.CyclePhase
import com.lunaflow.app.presentation.CycleIntent
import com.lunaflow.app.presentation.CycleViewModel
import com.lunaflow.app.presentation.components.*
import com.lunaflow.app.presentation.theme.LunaFlowTheme
import kotlinx.datetime.*

@Composable
fun App() {
    LunaFlowTheme {
        val scope = rememberCoroutineScope()

        val database = remember { getRoomDatabase() }
        val repository = remember { LunaRepositoryImpl(database.cycleDao()) }
        val preferencesRepository = remember { UserPreferencesRepository() }
        val cycleManager = remember { CycleManager() }
        val geminiService = remember { GeminiService(apiKey = com.lunaflow.app.BuildConfig.OPENAI_API_KEY) }

        val viewModel = remember {
            CycleViewModel(
                repository = repository,
                preferencesRepository = preferencesRepository,
                cycleManager = cycleManager,
                geminiService = geminiService,
                scope = scope
            )
        }

        val uiState by viewModel.uiState.collectAsState()
        val historyRecords by viewModel.historyRecords.collectAsState()

        // ── Onboarding Gate ───────────────────────────────────────
        if (!uiState.isOnboardingComplete) {
            OnboardingScreen(onComplete = { date, clen, plen ->
                viewModel.onIntent(CycleIntent.CompleteOnboarding(date, clen, plen))
            })
            return@LunaFlowTheme
        }

        var selectedTab by remember { mutableStateOf(0) }

        // ── Calendar: collect records for displayed month ─────────
        val calendarStart = remember(uiState.calendarYear, uiState.calendarMonth) {
            LocalDate(uiState.calendarYear, uiState.calendarMonth, 1)
        }
        val calendarEnd = remember(uiState.calendarYear, uiState.calendarMonth) {
            val daysInMonth = when (calendarStart.month) {
                Month.FEBRUARY -> if (isLeapYear(uiState.calendarYear)) 29 else 28
                Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
                else -> 31
            }
            LocalDate(uiState.calendarYear, uiState.calendarMonth, daysInMonth)
        }
        val calendarRecords by repository.getRecordsInRange(calendarStart, calendarEnd)
            .collectAsState(emptyList())

        Scaffold(
            bottomBar = {
                NavigationBar {
                    val tabs = listOf(
                        Triple(Icons.Default.Home,      "Today",    0),
                        Triple(Icons.Default.Face,      "Luna AI",  1),
                        Triple(Icons.Default.DateRange, "Log",      2),
                        Triple(Icons.Default.DateRange, "Calendar", 3),
                        Triple(Icons.Default.Star,      "Insights", 4)
                    )
                    tabs.forEach { (icon, label, index) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                when (selectedTab) {

                    // ─────────────── TAB 0: TODAY ─────────────────
                    0 -> {
                        uiState.status?.let { status ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(Modifier.height(16.dp))

                                // A4 — Luna Avatar with daily tip (A1)
                                LunaAvatarHeader(
                                    phase = status.currentPhase,
                                    dailyTip = uiState.dailyTip,
                                    onNextTip = { viewModel.onIntent(CycleIntent.NextDailyTip) }
                                )

                                Spacer(Modifier.height(16.dp))

                                // Hero cycle arc
                                HeroDashboard(
                                    phase = status.currentPhase,
                                    daysRemaining = status.daysUntilNextPeriod
                                )

                                Spacer(Modifier.height(16.dp))

                                // Quick phase info pills
                                PhaseInfoRow(
                                    ovulationDate = status.ovulationDate,
                                    nextPeriod = status.nextPeriodDate
                                )

                                Spacer(Modifier.height(16.dp))

                                // Wellness plan
                                Button(
                                    onClick = { viewModel.onIntent(CycleIntent.GenerateWellnessPlan) },
                                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                                ) {
                                    if (uiState.isLoading)
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    else Text("✨ Generate My Wellness Plan")
                                }

                                uiState.wellnessPlan?.let { plan ->
                                    Card(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                    ) {
                                        Text(
                                            text = plan,
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }

                    // ─────────────── TAB 1: AI TUTOR ──────────────
                    1 -> {
                        ChatInterface(
                            messages = uiState.chatMessages,
                            onSendMessage = { viewModel.onIntent(CycleIntent.SendChatMessage(it)) },
                            isLoading = uiState.isAiLoading,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // ─────────────── TAB 2: LOG ────────────────────
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(Modifier.height(12.dp))

                            // S3 — Mood Tracker
                            MoodTrackerCard(
                                selectedMoodScore = uiState.todayRecord?.moodScore ?: -1,
                                onMoodSelected = { viewModel.onIntent(CycleIntent.LogMood(it)) }
                            )

                            Spacer(Modifier.height(12.dp))

                            // S4 — Flow Intensity
                            val isMenstrual = uiState.status?.currentPhase == CyclePhase.MENSTRUAL
                            FlowIntensityCard(
                                selectedLevel = uiState.todayRecord?.flowIntensity ?: 0,
                                currentPhaseIsMenstrual = isMenstrual,
                                onLevelSelected = { viewModel.onIntent(CycleIntent.LogFlowIntensity(it)) }
                            )

                            Spacer(Modifier.height(12.dp))

                            // S1/S2 — Expanded Symptom Logger with severity
                            SymptomLogger(
                                onSymptomSelected = { symptom, severity ->
                                    viewModel.onIntent(
                                        CycleIntent.LogSymptomWithSeverity(symptom.id, severity)
                                    )
                                },
                                loggedSymptoms = uiState.todayRecord?.symptoms ?: emptyList()
                            )

                            Spacer(Modifier.height(12.dp))

                            // S5 — Water Intake
                            WaterIntakeCard(
                                cups = uiState.todayRecord?.waterCups ?: 0,
                                onAddCup = { viewModel.onIntent(CycleIntent.AddWaterCup) },
                                onRemoveCup = { viewModel.onIntent(CycleIntent.RemoveWaterCup) }
                            )

                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    // ─────────────── TAB 3: CALENDAR ──────────────
                    3 -> {
                        uiState.status?.let { status ->
                            CycleCalendarScreen(
                                cycleData = com.lunaflow.app.domain.manager.CycleData(
                                    lastPeriodDate = preferencesRepository.getLastPeriodDate()
                                        ?: kotlinx.datetime.Clock.System.todayIn(
                                            kotlinx.datetime.TimeZone.currentSystemDefault()
                                        ),
                                    cycleLength = preferencesRepository.getCycleLength(),
                                    periodLength = preferencesRepository.getPeriodLength()
                                ),
                                calendarYear = uiState.calendarYear,
                                calendarMonth = uiState.calendarMonth,
                                recordsForMonth = calendarRecords,
                                predictedPeriods = uiState.predictedPeriods,
                                cycleLengthHistory = uiState.cycleLengthHistory,
                                onPrevMonth = { viewModel.onIntent(CycleIntent.NavigateCalendarMonth(-1)) },
                                onNextMonth = { viewModel.onIntent(CycleIntent.NavigateCalendarMonth(+1)) },
                                onLogPeriodStart = { viewModel.onIntent(CycleIntent.LogPeriodStart) },
                                onLogPeriodEnd = { viewModel.onIntent(CycleIntent.LogPeriodEnd) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // ─────────────── TAB 4: INSIGHTS ──────────────
                    4 -> {
                        InsightsScreen(
                            symptomFrequency = uiState.symptomFrequency,
                            phaseSymptomCorrelation = uiState.phaseSymptomCorrelation,
                            moodHistory = uiState.moodHistory,
                            cycleLengthHistory = uiState.cycleLengthHistory,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhaseInfoRow(
    ovulationDate: kotlinx.datetime.LocalDate,
    nextPeriod: kotlinx.datetime.LocalDate
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoPill(
            emoji = "🌕",
            label = "Ovulation",
            value = "${ovulationDate.dayOfMonth} ${ovulationDate.month.name.take(3)}",
            modifier = Modifier.weight(1f)
        )
        InfoPill(
            emoji = "🩸",
            label = "Next Period",
            value = "${nextPeriod.dayOfMonth} ${nextPeriod.month.name.take(3)}",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoPill(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
