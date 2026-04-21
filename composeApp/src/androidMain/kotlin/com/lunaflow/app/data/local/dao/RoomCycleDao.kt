package com.lunaflow.app.data.local.dao

import androidx.room.*
import com.lunaflow.app.data.local.entity.CycleRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomCycleDao {
    @Query("SELECT * FROM CycleRecord WHERE date = :date")
    suspend fun getRecordForDate(date: String): CycleRecordEntity?

    @Query("SELECT * FROM CycleRecord ORDER BY date DESC")
    fun getAllRecords(): Flow<List<CycleRecordEntity>>

    @Query("SELECT * FROM CycleRecord WHERE date >= :start AND date <= :end ORDER BY date ASC")
    fun getRecordsInRange(start: String, end: String): Flow<List<CycleRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CycleRecordEntity)

    @Delete
    suspend fun deleteRecord(record: CycleRecordEntity)
}
