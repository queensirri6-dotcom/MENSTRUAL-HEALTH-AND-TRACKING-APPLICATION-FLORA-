package com.lunaflow.app.data.local.entity

import kotlinx.datetime.LocalDate

/** Pure cross-platform data class. Room @Entity annotation lives in CycleRecordEntity (androidMain). */
data class CycleRecord(
    val date: LocalDate,
    // F2 — Period flow tracking
    val flowLevel: Int = 0,           // 0=None, 1=Spotting, 2=Light, 3=Medium, 4=Heavy
    val isPeriodStart: Boolean = false,
    val isPeriodEnd: Boolean = false,
    // S1/S2 — Symptoms + per-symptom severity
    val symptoms: List<String> = emptyList(),
    val symptomSeverities: Map<String, Int> = emptyMap(), // symptomId -> 1..5
    // S3 — Mood
    val moodScore: Int = -1,           // -1=not logged, 0=😢..4=😄
    // S4 — Flow intensity
    val flowIntensity: Int = 0,
    // S5 — Water intake
    val waterCups: Int = 0,            // 0..8
    val mood: String? = null,
    val note: String? = null
)

