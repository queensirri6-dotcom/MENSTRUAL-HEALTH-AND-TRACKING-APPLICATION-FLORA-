package com.lunaflow.app.domain.manager

import com.lunaflow.app.data.local.entity.CycleRecord
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

    fun calculateCycleStatus(
        data: CycleData,
        today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    ): CycleStatus {
        val nextPeriodDate = data.lastPeriodDate.plus(data.cycleLength, DateTimeUnit.DAY)
        val daysUntilNextPeriod = today.daysUntil(nextPeriodDate)
        val ovulationDate = data.lastPeriodDate.plus(data.cycleLength - 14, DateTimeUnit.DAY)
        val fertilityWindow = ovulationDate.minus(5, DateTimeUnit.DAY)..ovulationDate.plus(1, DateTimeUnit.DAY)
        val daysSinceStart = data.lastPeriodDate.daysUntil(today)
        val phase = determinePhase(daysSinceStart, data.periodLength, today, fertilityWindow)

        return CycleStatus(
            currentPhase = phase,
            daysUntilNextPeriod = daysUntilNextPeriod,
            nextPeriodDate = nextPeriodDate,
            fertilityWindow = fertilityWindow,
            ovulationDate = ovulationDate
        )
    }

    // F1 — Get phase for any specific date (for calendar coloring)
    fun getPhaseForDate(date: LocalDate, data: CycleData): CyclePhase {
        val ovulationDate = data.lastPeriodDate.plus(data.cycleLength - 14, DateTimeUnit.DAY)
        val fertilityWindow = ovulationDate.minus(5, DateTimeUnit.DAY)..ovulationDate.plus(1, DateTimeUnit.DAY)
        val daysSinceStart = data.lastPeriodDate.daysUntil(date)
        // Handle previous cycles by rolling back
        val adjustedDays = ((daysSinceStart % data.cycleLength) + data.cycleLength) % data.cycleLength
        val adjustedDate = data.lastPeriodDate.plus(adjustedDays, DateTimeUnit.DAY)
        val adjustedOvulation = data.lastPeriodDate.plus(data.cycleLength - 14, DateTimeUnit.DAY)
        val adjustedFertilityWindow = adjustedOvulation.minus(5, DateTimeUnit.DAY)..adjustedOvulation.plus(1, DateTimeUnit.DAY)
        return determinePhase(adjustedDays, data.periodLength, adjustedDate, adjustedFertilityWindow)
    }

    // F5/F6 — Check if date is in fertility window or is ovulation day
    fun isInFertilityWindow(date: LocalDate, data: CycleData): Boolean {
        val ovulationDate = data.lastPeriodDate.plus(data.cycleLength - 14, DateTimeUnit.DAY)
        val window = ovulationDate.minus(5, DateTimeUnit.DAY)..ovulationDate.plus(1, DateTimeUnit.DAY)
        return date in window
    }

    fun isOvulationDay(date: LocalDate, data: CycleData): Boolean {
        val ovulationDate = data.lastPeriodDate.plus(data.cycleLength - 14, DateTimeUnit.DAY)
        return date == ovulationDate
    }

    // F4 — Get next N predicted period start dates
    fun getPredictedPeriods(data: CycleData, count: Int = 3): List<LocalDate> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val results = mutableListOf<LocalDate>()
        var candidate = data.lastPeriodDate
        while (results.size < count) {
            candidate = candidate.plus(data.cycleLength, DateTimeUnit.DAY)
            if (candidate >= today) results.add(candidate)
        }
        return results
    }

    // F3 — Compute cycle lengths from a list of period start dates
    fun getCycleLengthTrend(pastPeriodDates: List<LocalDate>): List<Int> {
        if (pastPeriodDates.size < 2) return emptyList()
        val sorted = pastPeriodDates.sortedBy { it }
        return sorted.zipWithNext { a, b -> a.daysUntil(b) }
    }

    // I2 — Which symptoms appear in which phases (for phase-symptom correlation)
    fun getSymptomPhaseCorrelation(
        records: List<CycleRecord>,
        data: CycleData
    ): Map<CyclePhase, List<String>> {
        val result = mutableMapOf<CyclePhase, MutableList<String>>()
        CyclePhase.values().forEach { result[it] = mutableListOf() }

        records.forEach { record ->
            val phase = getPhaseForDate(record.date, data)
            record.symptoms.forEach { symptom ->
                result[phase]?.let { list ->
                    if (!list.contains(symptom)) list.add(symptom)
                }
            }
        }
        return result
    }

    // I1 — Symptom frequency over last N records
    fun getSymptomFrequency(records: List<CycleRecord>): Map<String, Int> {
        val freq = mutableMapOf<String, Int>()
        records.forEach { record ->
            record.symptoms.forEach { s -> freq[s] = (freq[s] ?: 0) + 1 }
        }
        return freq.entries.sortedByDescending { it.value }.take(10).associate { it.toPair() }
    }

    fun analyzeSymptomTrends(records: List<SymptomRecord>, cycleData: CycleData): List<SymptomTrend> {
        return records.groupBy { it.symptomId }.map { (id, logs) ->
            val avgSeverity = logs.map { it.severity }.average().toFloat()
            SymptomTrend(
                symptomId = id,
                frequency = logs.size,
                averageSeverity = avgSeverity,
                phaseMostFrequent = null
            )
        }
    }

    private fun determinePhase(
        daysSinceStart: Int,
        periodLength: Int,
        date: LocalDate,
        fertilityWindow: ClosedRange<LocalDate>
    ): CyclePhase = when {
        daysSinceStart < 0        -> CyclePhase.LUTEAL
        daysSinceStart < periodLength -> CyclePhase.MENSTRUAL
        date < fertilityWindow.start  -> CyclePhase.FOLLICULAR
        date <= fertilityWindow.endInclusive -> CyclePhase.OVULATION
        else -> CyclePhase.LUTEAL
    }
}

