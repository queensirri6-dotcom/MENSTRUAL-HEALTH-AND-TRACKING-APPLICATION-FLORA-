package com.lunaflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity
data class CycleRecord(
    @PrimaryKey val date: LocalDate,
    val flowLevel: Int = 0, // 0: None, 1: Light, 2: Medium, 3: Heavy
    val symptoms: List<String> = emptyList(),
    val mood: String? = null,
    val note: String? = null
)
