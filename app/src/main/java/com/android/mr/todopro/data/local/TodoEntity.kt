package com.android.mr.todopro.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "todos")
data class TodoEntity (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "is_done") val isDone: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    // New field — tracks whether this item needs to be synced
    @ColumnInfo(name = "sync_status") val syncStatus: String = "LOCAL_ONLY"
)