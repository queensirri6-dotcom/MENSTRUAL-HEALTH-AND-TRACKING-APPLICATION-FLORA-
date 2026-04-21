package com.lunaflow.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

// Note: In a real app, you'd inject the context or use a provider.
private lateinit var appContext: Context

fun initContext(context: Context) {
    appContext = context
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<LunaDatabase> {
    val dbFile = appContext.getDatabasePath("luna.db")
    return Room.databaseBuilder<LunaDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
