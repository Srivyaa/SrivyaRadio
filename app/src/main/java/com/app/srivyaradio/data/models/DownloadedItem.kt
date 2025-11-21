package com.app.srivyaradio.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_items")
data class DownloadedItem(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val name: String,
    val countrycode: String,
    val sourceUrl: String,
    val fileUri: String,
    val image: String,
    val sizeBytes: Long,
    val createdAt: Long,
)
