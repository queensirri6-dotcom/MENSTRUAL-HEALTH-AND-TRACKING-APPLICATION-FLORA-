package com.lunaflow.app.data.local

import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(): RoomDatabase.Builder<LunaDatabase> {
    return Room.inMemoryDatabaseBuilder<LunaDatabase>()
}
