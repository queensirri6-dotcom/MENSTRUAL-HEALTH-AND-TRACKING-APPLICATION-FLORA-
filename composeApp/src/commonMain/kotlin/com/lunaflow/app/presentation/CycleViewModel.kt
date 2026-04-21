package com.lunaflow.app.presentation

import com.lunaflow.app.domain.manager.CycleData
import com.lunaflow.app.domain.manager.CycleManager
import com.lunaflow.app.domain.manager.CycleStatus
import com.lunaflow.app.domain.manager.DailyTipsManager
import com.lunaflow.app.domain.model.CyclePhase
import com.lunaflow.app.domain.repository.LunaRepository
import com.lunaflow.app.data.local.entity.CycleRecord
import com.lunaflow.app.data.repository.UserPreferencesRepository
import com.lunaflow.app.presentation.components.ChatMessage
import com.lunaflow.app.data.remote.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.minus
import kotlinx.datetime.DatePeriod

// ─────────────────────────────────────────────
// UI STATE
// ─────────────────────────────────────────────

data class CycleUiState(
    val status: CycleStatus? = null,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage("Hi! I'm Luna 🌙 Your personal cycle companion. How can I help you today?", false)
    ),
    val isAiLoading: Boolean = false,
    val educationMode: Boolean = true,
    val wellnessPlan: String? = null,
    val isLoading: Boolean = false,
    val isOnboardingComplete: Boolean = false,

    // A1 — Daily phase-aware tip
    val dailyTip: String = "",
    val dailyTipIndex: Int = 0,

    // F4 — Next predicted periods
    val predictedPeriods: List<LocalDate> = emptyList(),

    // F3 — Cycle length trend history
    val cycleLengthHistory: List<Int> = emptyList(),

    // I1 — Symptom frequency map
    val symptomFrequency: Map<String, Int> = emptyMap(),

    // I2 — Phase-to-symptom correlation
    val phaseSymptomCorrelation: Map<CyclePhase, List<String>> = emptyMap(),

    // I4 — Mood history: date → mood score
    val moodHistory: List<Pair<LocalDate, Int>> = emptyList(),

    // Calendar navigation
    val calendarYear: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year,
    val calendarMonth: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).monthNumber,

    // Today's log state (for quick access)
    val todayRecord: CycleRecord? = null
)

// ─────────────────────────────────────────────
// INTENTS
// ─────────────────────────────────────────────

sealed class CycleIntent {
    // Existing
    data class LogSymptom(val symptomId: String) : CycleIntent()
    data class SendChatMessage(val message: String) : CycleIntent()
    object GenerateWellnessPlan : CycleIntent()
    data class ToggleEducationMode(val enabled: Boolean) : CycleIntent()
    data class UpdateLastPeriodDate(val date: LocalDate) : CycleIntent()
    data class CompleteOnboarding(
        val lastPeriodDate: LocalDate,
        val cycleLength: Int,
        val periodLength: Int
    ) : CycleIntent()

    // New — S2: symptom with severity
    data class LogSymptomWithSeverity(val symptomId: String, val severity: Int) : CycleIntent()

    // New — S3: mood
    data class LogMood(val score: Int) : CycleIntent()

    // New — S4: flow intensity
    data class LogFlowIntensity(val level: Int) : CycleIntent()

    // New — S5: water
    object AddWaterCup : CycleIntent()
    object RemoveWaterCup : CycleIntent()

    // New — F2: period start / end
    object LogPeriodStart : CycleIntent()
    object LogPeriodEnd : CycleIntent()

    // New — A1: next/prev tip
    object NextDailyTip : CycleIntent()

    // New — Calendar
    data class NavigateCalendarMonth(val offset: Int) : CycleIntent() // +1 or -1
}

// ─────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────

class CycleViewModel(
    private val repository: LunaRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val cycleManager: CycleManager,
    private val geminiService: GeminiService,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(CycleUiState())
    val uiState: StateFlow<CycleUiState> = _uiState.asStateFlow()

    val historyRecords: StateFlow<List<CycleRecord>> = repository.getAllRecords()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var currentCycleData = CycleData(
        lastPeriodDate = preferencesRepository.getLastPeriodDate()
            ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(DatePeriod(days = 28)),
        cycleLength = preferencesRepository.getCycleLength(),
        periodLength = preferencesRepository.getPeriodLength()
    )

    init {
        val isComplete = preferencesRepository.isOnboardingComplete()
        _uiState.value = _uiState.value.copy(isOnboardingComplete = isComplete)
        if (isComplete) {
            refreshStatus()
            loadAnalytics()
            loadTodayRecord()
        }
        // Observe history for analytics
        scope.launch {
            historyRecords.collect { records ->
                updateAnalytics(records)
            }
        }
    }

    fun onIntent(intent: CycleIntent) {
        when (intent) {
            is CycleIntent.LogSymptom -> logSymptom(intent.symptomId, 3) // default severity 3
            is CycleIntent.LogSymptomWithSeverity -> logSymptom(intent.symptomId, intent.severity)
            is CycleIntent.SendChatMessage -> sendChatMessage(intent.message)
            is CycleIntent.GenerateWellnessPlan -> generateWellnessPlan()
            is CycleIntent.ToggleEducationMode -> _uiState.value = _uiState.value.copy(educationMode = intent.enabled)
            is CycleIntent.UpdateLastPeriodDate -> {
                currentCycleData = currentCycleData.copy(lastPeriodDate = intent.date)
                refreshStatus()
            }
            is CycleIntent.CompleteOnboarding -> {
                preferencesRepository.saveCyclePreferences(intent.lastPeriodDate, intent.cycleLength, intent.periodLength)
                currentCycleData = currentCycleData.copy(
                    lastPeriodDate = intent.lastPeriodDate,
                    cycleLength = intent.cycleLength,
                    periodLength = intent.periodLength
                )
                _uiState.value = _uiState.value.copy(isOnboardingComplete = true)
                refreshStatus()
                loadAnalytics()
                loadTodayRecord()
            }
            is CycleIntent.LogMood -> logMood(intent.score)
            is CycleIntent.LogFlowIntensity -> logFlowIntensity(intent.level)
            is CycleIntent.AddWaterCup -> adjustWater(+1)
            is CycleIntent.RemoveWaterCup -> adjustWater(-1)
            is CycleIntent.LogPeriodStart -> logPeriodStart()
            is CycleIntent.LogPeriodEnd -> logPeriodEnd()
            is CycleIntent.NextDailyTip -> rotateDailyTip()
            is CycleIntent.NavigateCalendarMonth -> navigateCalendar(intent.offset)
        }
    }

    // ─── Private helpers ───────────────────────────

    private fun refreshStatus() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val status = cycleManager.calculateCycleStatus(currentCycleData, today)
        val tipIndex = preferencesRepository.getDailyTipIndex()
        val tip = DailyTipsManager.getTipForPhase(status.currentPhase, tipIndex)
        val predicted = cycleManager.getPredictedPeriods(currentCycleData, 3)
        val history = preferencesRepository.getCycleHistory()
        val cycleLengths = cycleManager.getCycleLengthTrend(history)

        _uiState.value = _uiState.value.copy(
            status = status,
            dailyTip = tip,
            dailyTipIndex = tipIndex,
            predictedPeriods = predicted,
            cycleLengthHistory = cycleLengths
        )
    }

    private fun loadAnalytics() {
        scope.launch {
            val records = repository.getAllRecords().firstOrNull() ?: emptyList()
            updateAnalytics(records)
        }
    }

    private fun updateAnalytics(records: List<CycleRecord>) {
        val freqMap = cycleManager.getSymptomFrequency(records)
        val correlation = cycleManager.getSymptomPhaseCorrelation(records, currentCycleData)
        val moodHistory = records
            .filter { it.moodScore >= 0 }
            .sortedBy { it.date }
            .map { Pair(it.date, it.moodScore) }

        _uiState.value = _uiState.value.copy(
            symptomFrequency = freqMap,
            phaseSymptomCorrelation = correlation,
            moodHistory = moodHistory
        )
    }

    private fun loadTodayRecord() {
        scope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val record = repository.getRecordForDate(today)
            _uiState.value = _uiState.value.copy(todayRecord = record)
        }
    }

    private fun logSymptom(symptomId: String, severity: Int) {
        scope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val existing = repository.getRecordForDate(today)
            val newSymptoms = ((existing?.symptoms ?: emptyList()) + symptomId).distinct()
            val newSeverities = (existing?.symptomSeverities ?: emptyMap()).toMutableMap()
            newSeverities[symptomId] = severity
            val updated = (existing ?: CycleRecord(date = today)).copy(
                symptoms = newSymptoms,
                symptomSeverities = newSeverities
            )
            repository.saveRecord(updated)
            _uiState.value = _uiState.value.copy(todayRecord = updated)
        }
    }

    private fun logMood(score: Int) {
        scope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val existing = repository.getRecordForDate(today) ?: CycleRecord(date = today)
            val updated = existing.copy(moodScore = score)
            repository.saveRecord(updated)
            _uiState.value = _uiState.value.copy(todayRecord = updated)
        }
    }

    private fun logFlowIntensity(level: Int) {
        scope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val existing = repository.getRecordForDate(today) ?: CycleRecord(date = today)
            val updated = existing.copy(flowIntensity = level, flowLevel = level)
            repository.saveRecord(updated)
            _uiState.value = _uiState.value.copy(todayRecord = updated)
        }
    }

    private fun adjustWater(delta: Int) {
        scope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val existing = repository.getRecordForDate(today) ?: CycleRecord(date = today)
            val newCups = (existing.waterCups + delta).coerceIn(0, 8)
            val updated = existing.copy(waterCups = newCups)
            repository.saveRecord(updated)
            _uiState.value = _uiState.value.copy(todayRecord = updated)
        }
    }

    private fun logPeriodStart() {
        scope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val existing = repository.getRecordForDate(today) ?: CycleRecord(date = today)
            val updated = existing.copy(isPeriodStart = true, flowLevel = 2)
            repository.saveRecord(updated)
            _uiState.value = _uiState.value.copy(todayRecord = updated)
            // Update cycle data
            preferencesRepository.addPeriodStartDate(today)
            currentCycleData = currentCycleData.copy(lastPeriodDate = today)
            refreshStatus()
        }
    }

    private fun logPeriodEnd() {
        scope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val existing = repository.getRecordForDate(today) ?: CycleRecord(date = today)
            val updated = existing.copy(isPeriodEnd = true, flowLevel = 0)
            repository.saveRecord(updated)
            _uiState.value = _uiState.value.copy(todayRecord = updated)
        }
    }

    private fun rotateDailyTip() {
        val state = _uiState.value
        val phase = state.status?.currentPhase ?: CyclePhase.FOLLICULAR
        val nextIndex = (state.dailyTipIndex + 1) % DailyTipsManager.getTipCount(phase)
        val nextTip = DailyTipsManager.getTipForPhase(phase, nextIndex)
        preferencesRepository.saveDailyTipIndex(nextIndex)
        _uiState.value = state.copy(dailyTip = nextTip, dailyTipIndex = nextIndex)
    }

    private fun navigateCalendar(offset: Int) {
        val state = _uiState.value
        var month = state.calendarMonth + offset
        var year = state.calendarYear
        if (month > 12) { month = 1; year++ }
        if (month < 1) { month = 12; year-- }
        _uiState.value = state.copy(calendarMonth = month, calendarYear = year)
    }

    private fun sendChatMessage(text: String) {
        val currentMessages = _uiState.value.chatMessages
        _uiState.value = _uiState.value.copy(
            chatMessages = currentMessages + ChatMessage(text, true),
            isAiLoading = true
        )
        scope.launch(Dispatchers.Default) {
            // A2 — Build rich context with phase, symptoms, mood
            val state = _uiState.value
            var contextString = if (state.educationMode) "User is a teen girl. Keep language simple and supportive. " else ""
            state.status?.let { status ->
                contextString += "Current cycle phase: ${status.currentPhase.displayName}. " +
                    "Days until next period: ${status.daysUntilNextPeriod}. "
            }
            try {
                val latestRecords = repository.getAllRecords().firstOrNull() ?: emptyList()
                val recentLogs = latestRecords.take(7).joinToString("; ") { r ->
                    val moodStr = if (r.moodScore >= 0) " mood:${r.moodScore}" else ""
                    "Date ${r.date}: ${r.symptoms.joinToString()}$moodStr"
                }
                if (recentLogs.isNotEmpty()) contextString += "Recent logs: $recentLogs."
            } catch (e: Exception) { /* ignore */ }

            val response = geminiService.askHealthQuestion(text, contextString)
            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + ChatMessage(
                    response ?: "I'm having a little trouble connecting. Please try again! 🌙",
                    false
                ),
                isAiLoading = false
            )
        }
    }

    private fun generateWellnessPlan() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        scope.launch(Dispatchers.Default) {
            val phase = _uiState.value.status?.currentPhase?.displayName ?: "Unknown"
            val dataString = "Last period: ${currentCycleData.lastPeriodDate}, Phase: $phase, " +
                "Cycle length: ${currentCycleData.cycleLength} days"
            val plan = geminiService.generateWellnessPlan(dataString)
            _uiState.value = _uiState.value.copy(wellnessPlan = plan, isLoading = false)
        }
    }
}
