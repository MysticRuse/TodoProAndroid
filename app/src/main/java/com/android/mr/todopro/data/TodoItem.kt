package com.android.mr.todopro.data

data class TodoItem (
    val id: Int,
    val text: String,
    val isDone: Boolean = false
)