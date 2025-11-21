package com.app.srivyaradio.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.srivyaradio.data.models.Favorite
import com.app.srivyaradio.data.models.Station
import com.app.srivyaradio.data.models.DownloadedItem
import com.app.srivyaradio.utils.Constants.DATABASE

@Database(
    entities = [Station::class, Favorite::class, DownloadedItem::class], version = 2
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
            ).addMigrations(MIGRATION_1_2).build()
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
    }
}