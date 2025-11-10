package com.app.srivyaradio.data.models

/**
 * Represents a user-managed country/category entry.
 * code is the unique key (case-insensitive). active=false indicates soft-deleted.
 */
data class CountryEntry(
    val name: String,
    val code: String,
    val active: Boolean = true
)
