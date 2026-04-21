package com.lunaflow.app.domain.repository

import com.lunaflow.app.data.local.entity.CycleRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface LunaRepository {
    fun getAllRecords(): Flow<List<CycleRecord>>
    fun getRecordsInRange(start: LocalDate, end: LocalDate): Flow<List<CycleRecord>>
    suspend fun getRecordForDate(date: LocalDate): CycleRecord?
    suspend fun saveRecord(record: CycleRecord)
}

