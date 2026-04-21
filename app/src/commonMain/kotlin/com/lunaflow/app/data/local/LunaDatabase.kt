package com.lunaflow.app.data.local

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lunaflow.app.data.local.dao.CycleDao
import com.lunaflow.app.data.local.entity.CycleRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [CycleRecord::class], version = 1)
@TypeConverters(LunaConverters::class)
abstract class LunaDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<LunaDatabase>
): LunaDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
