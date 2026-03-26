package com.android.mr.todopro.data.mappers

import com.android.mr.todopro.data.local.TodoEntity
import com.android.mr.todopro.data.remote.TodoDto
import com.android.mr.todopro.domain.SyncStatus
import com.android.mr.todopro.domain.TodoItem

// Entity  -> Domain (DB row becomes the business object)
fun TodoEntity.toDomain() =  TodoItem(
    id = id,
    userId = userId,
    text = text,
    isDone = isDone,
    // Include the SyncStatus
    syncStatus = SyncStatus.valueOf(syncStatus)
)

// Domain -> Entity (Business object becomes a DB row)
fun TodoItem.toEntity() = TodoEntity(
    id = id,
    userId = userId,
    text = text,
    isDone = isDone
)

// DTO -> Entity (what comes from the server goes directly to the DB
fun TodoDto.toEntity() = TodoEntity(
    id = id,
    userId = userId,
    text = title,
    isDone = completed,
    syncStatus = SyncStatus.SYNCED.name
)

// Domain -> DTO (to send to server)
fun TodoItem.toDto() = TodoDto(
    id = id,
    userId = userId,
    title = text,
    completed = isDone
)

