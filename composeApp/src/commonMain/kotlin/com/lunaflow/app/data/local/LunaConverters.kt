package com.lunaflow.app.data.local

import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Pure Kotlin helpers used by both commonMain and androidMain. */
object LunaConverters {
    fun localDateToString(value: LocalDate?): String? = value?.toString()
    fun stringToLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
    fun stringListToJson(value: List<String>): String = Json.encodeToString(value)
    fun jsonToStringList(value: String): List<String> = Json.decodeFromString(value)
    fun stringIntMapToJson(value: Map<String, Int>): String = Json.encodeToString(value)
    fun jsonToStringIntMap(value: String): Map<String, Int> = Json.decodeFromString(value)
}

