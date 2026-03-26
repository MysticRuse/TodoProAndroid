package com.android.mr.todopro.domain

data class TodoItem (
    val id: Int,
    val userId: Int,
    val text: String,
    val isDone: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
)