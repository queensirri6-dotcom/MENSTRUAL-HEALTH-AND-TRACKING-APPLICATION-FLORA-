package com.lunaflow.app.data.local

import androidx.room.*
import com.lunaflow.app.data.local.dao.RoomCycleDao
import com.lunaflow.app.data.local.entity.CycleRecordEntity
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Database(entities = [CycleRecordEntity::class], version = 2)
abstract class LunaRoomDatabase : RoomDatabase() {
    abstract fun roomCycleDao(): RoomCycleDao
}
