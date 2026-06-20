package com.ncert7.aitutorandlab.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE app_analytics ADD COLUMN conceptId TEXT")
        db.execSQL("ALTER TABLE app_analytics ADD COLUMN source TEXT")
        db.execSQL("ALTER TABLE app_analytics ADD COLUMN interactionType TEXT")
    }
}
