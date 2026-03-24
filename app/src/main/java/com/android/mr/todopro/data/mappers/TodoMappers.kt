package com.android.mr.todopro.data.mappers

import com.android.mr.todopro.data.local.TodoEntity
import com.android.mr.todopro.domain.TodoItem

// Entity  -> Domain (DB row becomes the business object)
fun TodoEntity.toDomain() =  TodoItem(
    id = id,
    text = text,
    isDone = isDone
)

// Domain -> Entity (Business object becomes a DB row)
fun TodoItem.toEntity() = TodoEntity(
    id = id,
    text = text,
    isDone = isDone
)