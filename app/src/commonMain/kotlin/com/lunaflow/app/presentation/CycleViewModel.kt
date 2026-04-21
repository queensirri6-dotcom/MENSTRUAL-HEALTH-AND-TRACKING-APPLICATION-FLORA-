package com.lunaflow.app.presentation

import com.lunaflow.app.domain.manager.CycleData
import com.lunaflow.app.domain.manager.CycleManager
import com.lunaflow.app.domain.manager.CycleStatus
import com.lunaflow.app.domain.repository.LunaRepository
import com.lunaflow.app.data.local.entity.CycleRecord
import com.lunaflow.app.presentation.components.ChatMessage
import com.lunaflow.app.data.remote.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class CycleUiState(
    val status: CycleStatus? = null,
    val chatMessages: List<ChatMessage> = listOf(ChatMessage("Hi! I'm Luna, your AI Health Tutor. How can I help you today?", false)),
    val isAiLoading: Boolean = false,
    val educationMode: Boolean = true,
    val wellnessPlan: String? = null,
    val isLoading: Boolean = false
)

sealed class CycleIntent {
    data class LogSymptom(val symptomId: String) : CycleIntent()
    data class SendChatMessage(val message: String) : CycleIntent()
    object GenerateWellnessPlan : CycleIntent()
    data class ToggleEducationMode(val enabled: Boolean) : CycleIntent()
    data class UpdateLastPeriodDate(val date: LocalDate) : CycleIntent()
}

class CycleViewModel(
    private val repository: LunaRepository,
    private val cycleManager: CycleManager,
    private val geminiService: GeminiService,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(CycleUiState())
    val uiState: StateFlow<CycleUiState> = _uiState.asStateFlow()

    private var currentCycleData = CycleData(
        lastPeriodDate = Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(kotlinx.datetime.DateTimeUnit.DAY * 28)
    )

    init {
        refreshStatus()
    }

    fun onIntent(intent: CycleIntent) {
        when (intent) {
            is CycleIntent.LogSymptom -> logSymptom(intent.symptomId)
            is CycleIntent.SendChatMessage -> sendChatMessage(intent.message)
            is CycleIntent.GenerateWellnessPlan -> generateWellnessPlan()
            is CycleIntent.ToggleEducationMode -> _uiState.value = _uiState.value.copy(educationMode = intent.enabled)
            is CycleIntent.UpdateLastPeriodDate -> {
                currentCycleData = currentCycleData.copy(lastPeriodDate = intent.date)
                refreshStatus()
            }
        }
    }

    private fun refreshStatus() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val status = cycleManager.calculateCycleStatus(currentCycleData, today)
        _uiState.value = _uiState.value.copy(status = status)
    }

    private fun logSymptom(symptomId: String) {
        scope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val existing = repository.getRecordForDate(today)
            val newSymptoms = (existing?.symptoms ?: emptyList()) + symptomId
            repository.saveRecord(
                CycleRecord(
                    date = today,
                    symptoms = newSymptoms.distinct()
                )
            )
        }
    }

    private fun sendChatMessage(text: String) {
        val currentMessages = _uiState.value.chatMessages
        _uiState.value = _uiState.value.copy(
            chatMessages = currentMessages + ChatMessage(text, true),
            isAiLoading = true
        )

        scope.launch(Dispatchers.Default) {
            val history = if (_uiState.value.educationMode) "User is a teen girl. Keep language simple and supportive." else ""
            val response = geminiService.askHealthQuestion(text, history)
            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + ChatMessage(response ?: "I'm having a little trouble. Try again?", false),
                isAiLoading = false
            )
        }
    }

    private fun generateWellnessPlan() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        scope.launch(Dispatchers.Default) {
            val dataString = "Last period: ${currentCycleData.lastPeriodDate}, Phase: ${_uiState.value.status?.currentPhase}"
            val plan = geminiService.generateWellnessPlan(dataString)
            _uiState.value = _uiState.value.copy(wellnessPlan = plan, isLoading = false)
        }
    }
}
