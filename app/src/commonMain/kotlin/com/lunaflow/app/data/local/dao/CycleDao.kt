package com.lunaflow.app.data.local.dao

import androidx.room.*
import com.lunaflow.app.data.local.entity.CycleRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface CycleDao {
    @Query("SELECT * FROM CycleRecord WHERE date = :date")
    suspend fun getRecordForDate(date: LocalDate): CycleRecord?

    @Query("SELECT * FROM CycleRecord ORDER BY date DESC")
    fun getAllRecords(): Flow<List<CycleRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CycleRecord)

    @Delete
    suspend fun deleteRecord(record: CycleRecord)
}
