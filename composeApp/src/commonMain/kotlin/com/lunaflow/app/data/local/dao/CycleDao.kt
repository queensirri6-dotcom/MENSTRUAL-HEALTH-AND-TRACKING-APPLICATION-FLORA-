package com.lunaflow.app.data.local.dao

import com.lunaflow.app.data.local.entity.CycleRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Cross-platform DAO contract. Room @Dao implementation is in androidMain. */
interface CycleDao {
    suspend fun getRecordForDate(date: LocalDate): CycleRecord?
    fun getAllRecords(): Flow<List<CycleRecord>>
    fun getRecordsInRange(start: LocalDate, end: LocalDate): Flow<List<CycleRecord>>
    suspend fun insertRecord(record: CycleRecord)
    suspend fun deleteRecord(record: CycleRecord)
}

