package com.lunaflow.app.domain.manager

import com.lunaflow.app.domain.model.CyclePhase
import kotlinx.datetime.*

data class CycleData(
    val lastPeriodDate: LocalDate,
    val cycleLength: Int = 28,
    val periodLength: Int = 5
)

data class CycleStatus(
    val currentPhase: CyclePhase,
    val daysUntilNextPeriod: Int,
    val nextPeriodDate: LocalDate,
    val fertilityWindow: ClosedRange<LocalDate>,
    val ovulationDate: LocalDate
)

data class SymptomRecord(
    val date: LocalDate,
    val symptomId: String,
    val severity: Int // 1-5
)

data class SymptomTrend(
    val symptomId: String,
    val frequency: Int,
    val averageSeverity: Float,
    val phaseMostFrequent: CyclePhase?
)

class CycleManager {
    fun calculateCycleStatus(data: CycleData, today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): CycleStatus {
        val nextPeriodDate = data.lastPeriodDate.plus(data.cycleLength, DateTimeUnit.DAY)
        val daysUntilNextPeriod = today.daysUntil(nextPeriodDate)
        
        val ovulationDate = data.lastPeriodDate.plus(data.cycleLength - 14, DateTimeUnit.DAY)
        val fertilityWindow = ovulationDate.minus(5, DateTimeUnit.DAY)..ovulationDate.plus(1, DateTimeUnit.DAY)
        
        val daysSinceStart = data.lastPeriodDate.daysUntil(today)
        val phase = when {
            daysSinceStart < 0 -> CyclePhase.LUTEAL // Simplified: assuming previous cycle
            daysSinceStart < data.periodLength -> CyclePhase.MENSTRUAL
            today < fertilityWindow.start -> CyclePhase.FOLLICULAR
            today <= fertilityWindow.endInclusive -> CyclePhase.OVULATION
            else -> CyclePhase.LUTEAL
        }
        
        return CycleStatus(
            currentPhase = phase,
            daysUntilNextPeriod = daysUntilNextPeriod,
            nextPeriodDate = nextPeriodDate,
            fertilityWindow = fertilityWindow,
            ovulationDate = ovulationDate
        )
    }

    fun analyzeSymptomTrends(records: List<SymptomRecord>, cycleData: CycleData): List<SymptomTrend> {
        return records.groupBy { it.symptomId }.map { (id, logs) ->
            val avgSeverity = logs.map { it.severity }.average().toFloat()
            // In a real app, we'd map each log date to a CyclePhase using calculateCycleStatus
            SymptomTrend(
                symptomId = id,
                frequency = logs.size,
                averageSeverity = avgSeverity,
                phaseMostFrequent = null // Placeholder for complex logic
            )
        }
    }
}
