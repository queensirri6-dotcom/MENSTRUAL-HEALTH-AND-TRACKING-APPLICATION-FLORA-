package com.lunaflow.app.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lunaflow.app.data.local.dao.CycleDao
import com.lunaflow.app.data.local.dao.RoomCycleDao
import com.lunaflow.app.data.local.entity.CycleRecord
import com.lunaflow.app.data.local.entity.CycleRecordEntity
import com.lunaflow.app.data.local.entity.toDomain
import com.lunaflow.app.data.local.entity.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

private lateinit var appContext: android.content.Context

fun initContext(context: android.content.Context) {
    appContext = context
}

actual class LunaDatabase(private val roomDb: LunaRoomDatabase) {
    actual fun cycleDao(): CycleDao = CycleDaoWrapper(roomDb.roomCycleDao())
}

actual fun getRoomDatabase(): LunaDatabase {
    val dbFile = appContext.getDatabasePath("luna.db")
    val roomDb = Room.databaseBuilder<LunaRoomDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
        .fallbackToDestructiveMigration(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    return LunaDatabase(roomDb)
}

actual fun getDatabaseBuilder(): LunaDatabase = getRoomDatabase()

private class CycleDaoWrapper(private val roomDao: RoomCycleDao) : CycleDao {
    override suspend fun getRecordForDate(date: LocalDate): CycleRecord? {
        return roomDao.getRecordForDate(date.toString())?.toDomain(
            { LocalDate.parse(it) },
            { LunaConverters.jsonToStringList(it) },
            { LunaConverters.jsonToStringIntMap(it) }
        )
    }

    override fun getAllRecords(): Flow<List<CycleRecord>> {
        return roomDao.getAllRecords().map { list ->
            list.map {
                it.toDomain(
                    { d -> LocalDate.parse(d) },
                    { s -> LunaConverters.jsonToStringList(s) },
                    { m -> LunaConverters.jsonToStringIntMap(m) }
                )
            }
        }
    }

    override fun getRecordsInRange(start: LocalDate, end: LocalDate): Flow<List<CycleRecord>> {
        return roomDao.getRecordsInRange(start.toString(), end.toString()).map { list ->
            list.map {
                it.toDomain(
                    { d -> LocalDate.parse(d) },
                    { s -> LunaConverters.jsonToStringList(s) },
                    { m -> LunaConverters.jsonToStringIntMap(m) }
                )
            }
        }
    }

    override suspend fun insertRecord(record: CycleRecord) {
        roomDao.insertRecord(
            record.toEntity(
                { it.toString() },
                { LunaConverters.stringListToJson(it) },
                { LunaConverters.stringIntMapToJson(it) }
            )
        )
    }

    override suspend fun deleteRecord(record: CycleRecord) {
        roomDao.deleteRecord(
            record.toEntity(
                { it.toString() },
                { LunaConverters.stringListToJson(it) },
                { LunaConverters.stringIntMapToJson(it) }
            )
        )
    }
}
