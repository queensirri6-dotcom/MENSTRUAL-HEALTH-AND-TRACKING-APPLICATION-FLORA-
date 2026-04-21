package com.lunaflow.app.data.repository

import com.lunaflow.app.data.local.dao.CycleDao
import com.lunaflow.app.data.local.entity.CycleRecord
import com.lunaflow.app.domain.repository.LunaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class LunaRepositoryImpl(private val cycleDao: CycleDao) : LunaRepository {
    override fun getAllRecords(): Flow<List<CycleRecord>> = cycleDao.getAllRecords()
    
    override suspend fun getRecordForDate(date: LocalDate): CycleRecord? {
        return cycleDao.getRecordForDate(date)
    }
    
    override suspend fun saveRecord(record: CycleRecord) {
        cycleDao.insertRecord(record)
    }
}
