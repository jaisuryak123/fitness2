package com.musclegain.tracker.core.data.backup

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.withTransaction
import com.musclegain.tracker.core.data.DATABASE_VERSION
import com.musclegain.tracker.core.data.MuscleGainDatabase
import com.musclegain.tracker.core.domain.model.CsvTable
import com.musclegain.tracker.core.domain.repository.BackupRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupRepositoryImpl(private val database: MuscleGainDatabase) : BackupRepository {

    override suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val db = database.openHelper.readableDatabase
        val tables = JSONObject()
        TABLES.forEach { table ->
            val rows = JSONArray()
            db.query("SELECT * FROM $table ORDER BY rowid").use { cursor ->
                while (cursor.moveToNext()) rows.put(cursor.toJsonRow())
            }
            tables.put(table, rows)
        }
        JSONObject()
            .put("app", APP_NAME)
            .put("backupVersion", BACKUP_VERSION)
            .put("schemaVersion", DATABASE_VERSION)
            .put("exportedAtMillis", System.currentTimeMillis())
            .put("tables", tables)
            .toString()
    }

    override suspend fun restoreJson(json: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(json)
            require(root.optString("app") == APP_NAME) { "Not a $APP_NAME backup" }
            require(root.optInt("backupVersion", -1) == BACKUP_VERSION) { "Unsupported backup version" }
            require(root.optInt("schemaVersion", -1) == DATABASE_VERSION) { "Backup schema does not match this app version" }
            val tables = root.getJSONObject("tables")
            val rows = TABLES.associateWith { tables.getJSONArray(it) }
            database.withTransaction {
                val db = database.openHelper.writableDatabase
                TABLES.reversed().forEach { db.execSQL("DELETE FROM $it") }
                TABLES.forEach { table ->
                    val array = rows.getValue(table)
                    for (i in 0 until array.length()) {
                        db.insert(table, SQLiteDatabase.CONFLICT_ABORT, array.getJSONObject(i).toContentValues())
                    }
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportCsv(table: CsvTable): String = withContext(Dispatchers.IO) {
        val (name, order) = CSV_SOURCES.getValue(table)
        val db = database.openHelper.readableDatabase
        val out = StringBuilder()
        db.query("SELECT * FROM $name ORDER BY $order").use { cursor ->
            val columns = 0 until cursor.columnCount
            out.append(columns.joinToString(",") { cursor.getColumnName(it) }).append('\n')
            while (cursor.moveToNext()) {
                out.append(columns.joinToString(",") { csvField(if (cursor.isNull(it)) "" else cursor.getString(it)) })
                    .append('\n')
            }
        }
        out.toString()
    }

    override suspend fun deleteAllData() {
        withContext(Dispatchers.IO) { database.clearAllTables() }
    }

    private fun csvField(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else value

    private fun Cursor.toJsonRow(): JSONObject {
        val row = JSONObject()
        for (i in 0 until columnCount) {
            val name = getColumnName(i)
            when (getType(i)) {
                Cursor.FIELD_TYPE_NULL -> row.put(name, JSONObject.NULL)
                Cursor.FIELD_TYPE_INTEGER -> row.put(name, getLong(i))
                Cursor.FIELD_TYPE_FLOAT -> row.put(name, getDouble(i))
                else -> row.put(name, getString(i))
            }
        }
        return row
    }

    private fun JSONObject.toContentValues(): ContentValues {
        val values = ContentValues()
        keys().forEach { key ->
            if (isNull(key)) {
                values.putNull(key)
            } else {
                when (val value = get(key)) {
                    is Int -> values.put(key, value.toLong())
                    is Long -> values.put(key, value)
                    is Double -> values.put(key, value)
                    is Boolean -> values.put(key, if (value) 1L else 0L)
                    else -> values.put(key, value.toString())
                }
            }
        }
        return values
    }

    private companion object {
        const val APP_NAME = "MuscleGainTracker"
        const val BACKUP_VERSION = 1

        val TABLES = listOf(
            "user_profile", "daily_goals", "nutrition_entries", "workout_sessions", "workout_exercises",
            "supplement_entries", "body_weight_entries", "training_schedule", "reminder_settings"
        )

        val CSV_SOURCES = mapOf(
            CsvTable.NUTRITION to ("nutrition_entries" to "entryDate, entryTime, id"),
            CsvTable.WORKOUTS to ("workout_sessions" to "activityDate, id"),
            CsvTable.EXERCISES to ("workout_exercises" to "sessionId, id"),
            CsvTable.BODY_WEIGHT to ("body_weight_entries" to "recordedDate"),
            CsvTable.SUPPLEMENTS to ("supplement_entries" to "entryDate, entryTime, id")
        )
    }
}
