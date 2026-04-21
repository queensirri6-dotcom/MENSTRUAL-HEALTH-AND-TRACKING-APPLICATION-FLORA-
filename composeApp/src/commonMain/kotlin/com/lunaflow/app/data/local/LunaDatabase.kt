package com.lunaflow.app.data.local

import com.lunaflow.app.data.local.dao.CycleDao

/**
 * Expect: Cross-platform abstract database handle.
 * Actual implementations live in androidMain (Room/SQLite) and wasmJsMain (in-memory).
 */
expect class LunaDatabase {
    fun cycleDao(): CycleDao
}

expect fun getRoomDatabase(): LunaDatabase
expect fun getDatabaseBuilder(): LunaDatabase  // kept for compat, delegates to getRoomDatabase

