package com.lunaflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lunaflow.app.data.local.entity.CycleRecord
import kotlinx.datetime.LocalDate

@Entity(tableName = "CycleRecord")
data class CycleRecordEntity(
    @PrimaryKey val date: String,
    val flowLevel: Int,
    val isPeriodStart: Boolean,
    val isPeriodEnd: Boolean,
    val symptoms: String, // stored as json
    val symptomSeverities: String, // stored as json
    val moodScore: Int,
    val flowIntensity: Int,
    val waterCups: Int,
    val mood: String?,
    val note: String?
)

fun CycleRecordEntity.toDomain(
    parseDate: (String) -> LocalDate,
    parseList: (String) -> List<String>,
    parseMap: (String) -> Map<String, Int>
): CycleRecord = CycleRecord(
    date = parseDate(date),
    flowLevel = flowLevel,
    isPeriodStart = isPeriodStart,
    isPeriodEnd = isPeriodEnd,
    symptoms = parseList(symptoms),
    symptomSeverities = parseMap(symptomSeverities),
    moodScore = moodScore,
    flowIntensity = flowIntensity,
    waterCups = waterCups,
    mood = mood,
    note = note
)

fun CycleRecord.toEntity(
    formatDate: (LocalDate) -> String,
    formatList: (List<String>) -> String,
    formatMap: (Map<String, Int>) -> String
): CycleRecordEntity = CycleRecordEntity(
    date = formatDate(date),
    flowLevel = flowLevel,
    isPeriodStart = isPeriodStart,
    isPeriodEnd = isPeriodEnd,
    symptoms = formatList(symptoms),
    symptomSeverities = formatMap(symptomSeverities),
    moodScore = moodScore,
    flowIntensity = flowIntensity,
    waterCups = waterCups,
    mood = mood,
    note = note
)
