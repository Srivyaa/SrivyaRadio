package com.app.srivyaradio.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.srivyaradio.data.models.Favorite
import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.data.models.UnifiedStation
import com.app.srivyaradio.data.models.DownloadedItem
import com.app.srivyaradio.utils.Constants.DATABASE

@Database(
    entities = [Station::class, Favorite::class, UnifiedStation::class, DownloadedItem::class], version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun radioStationDao(): EntityDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, DATABASE
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS downloaded_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        countrycode TEXT NOT NULL,
                        sourceUrl TEXT NOT NULL,
                        fileUri TEXT NOT NULL,
                        image TEXT NOT NULL,
                        sizeBytes INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop the old table if it exists (to handle previous incorrect migrations)
                database.execSQL("DROP TABLE IF EXISTS unified_stations")
                
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS unified_stations (
                        id TEXT NOT NULL PRIMARY KEY DEFAULT 'undefined',
                        favicon TEXT NOT NULL DEFAULT '',
                        name TEXT NOT NULL DEFAULT '',
                        country TEXT NOT NULL DEFAULT '',
                        tags TEXT NOT NULL DEFAULT '0',
                        countrycode TEXT NOT NULL DEFAULT '',
                        url_resolved TEXT NOT NULL DEFAULT '',
                        state TEXT NOT NULL DEFAULT '',
                        homepage TEXT NOT NULL DEFAULT '',
                        rank INTEGER NOT NULL DEFAULT 0,
                        title TEXT NOT NULL DEFAULT '',
                        album TEXT NOT NULL DEFAULT '',
                        artist TEXT NOT NULL DEFAULT '',
                        year INTEGER NOT NULL DEFAULT 0,
                        url TEXT NOT NULL DEFAULT '',
                        favurl TEXT NOT NULL DEFAULT '',
                        language TEXT NOT NULL DEFAULT '',
                        bitrate INTEGER NOT NULL DEFAULT 0,
                        codec TEXT NOT NULL DEFAULT '',
                        votes INTEGER NOT NULL DEFAULT 0,
                        negativeVotes INTEGER NOT NULL DEFAULT 0,
                        clickCount INTEGER NOT NULL DEFAULT 0,
                        lastCheckOk INTEGER NOT NULL DEFAULT 1,
                        lastCheckTime TEXT NOT NULL DEFAULT '',
                        clickTimestamp TEXT NOT NULL DEFAULT '',
                        changeUuid TEXT NOT NULL DEFAULT '',
                        serverUuid TEXT NOT NULL DEFAULT '',
                        sourceType TEXT NOT NULL DEFAULT 'regular'
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Recreate the unified_stations table with correct NOT NULL constraint on id
                // First, create a temporary table with the correct schema
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS unified_stations_new (
                        id TEXT NOT NULL PRIMARY KEY DEFAULT 'undefined',
                        favicon TEXT NOT NULL DEFAULT '',
                        name TEXT NOT NULL DEFAULT '',
                        country TEXT NOT NULL DEFAULT '',
                        tags TEXT NOT NULL DEFAULT '0',
                        countrycode TEXT NOT NULL DEFAULT '',
                        url_resolved TEXT NOT NULL DEFAULT '',
                        state TEXT NOT NULL DEFAULT '',
                        homepage TEXT NOT NULL DEFAULT '',
                        rank INTEGER NOT NULL DEFAULT 0,
                        title TEXT NOT NULL DEFAULT '',
                        album TEXT NOT NULL DEFAULT '',
                        artist TEXT NOT NULL DEFAULT '',
                        year INTEGER NOT NULL DEFAULT 0,
                        url TEXT NOT NULL DEFAULT '',
                        favurl TEXT NOT NULL DEFAULT '',
                        language TEXT NOT NULL DEFAULT '',
                        bitrate INTEGER NOT NULL DEFAULT 0,
                        codec TEXT NOT NULL DEFAULT '',
                        votes INTEGER NOT NULL DEFAULT 0,
                        negativeVotes INTEGER NOT NULL DEFAULT 0,
                        clickCount INTEGER NOT NULL DEFAULT 0,
                        lastCheckOk INTEGER NOT NULL DEFAULT 1,
                        lastCheckTime TEXT NOT NULL DEFAULT '',
                        clickTimestamp TEXT NOT NULL DEFAULT '',
                        changeUuid TEXT NOT NULL DEFAULT '',
                        serverUuid TEXT NOT NULL DEFAULT '',
                        sourceType TEXT NOT NULL DEFAULT 'regular'
                    )
                    """.trimIndent()
                )
                
                // Copy data from old table to new table (only rows with non-null id)
                database.execSQL(
                    """
                    INSERT INTO unified_stations_new 
                    SELECT * FROM unified_stations WHERE id IS NOT NULL
                    """.trimIndent()
                )
                
                // Drop the old table
                database.execSQL("DROP TABLE unified_stations")
                
                // Rename the new table to the original name
                database.execSQL("ALTER TABLE unified_stations_new RENAME TO unified_stations")
            }
        }
    }
}