package com.lunaflow.app.data.local

import com.lunaflow.app.data.local.dao.CycleDao
import com.lunaflow.app.data.local.entity.CycleRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

// In-memory fallback for WASM since Room/SQLite is not available

actual class LunaDatabase {
    private val inMemoryDao = InMemoryCycleDao()
    actual fun cycleDao(): CycleDao = inMemoryDao
}

actual fun getRoomDatabase(): LunaDatabase = LunaDatabase()
actual fun getDatabaseBuilder(): LunaDatabase = getRoomDatabase()

private class InMemoryCycleDao : CycleDao {
    private val records = MutableStateFlow<Map<LocalDate, CycleRecord>>(emptyMap())

    override suspend fun getRecordForDate(date: LocalDate): CycleRecord? {
        return records.value[date]
    }

    override fun getAllRecords(): Flow<List<CycleRecord>> {
        return records.map { map -> map.values.sortedByDescending { it.date } }
    }

    override fun getRecordsInRange(start: LocalDate, end: LocalDate): Flow<List<CycleRecord>> {
        return records.map { map ->
            map.values.filter { it.date in start..end }.sortedBy { it.date }
        }
    }

    override suspend fun insertRecord(record: CycleRecord) {
        val current = records.value.toMutableMap()
        current[record.date] = record
        records.value = current
    }

    override suspend fun deleteRecord(record: CycleRecord) {
        val current = records.value.toMutableMap()
        current.remove(record.date)
        records.value = current
    }
}
