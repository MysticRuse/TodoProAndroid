package com.android.mr.todopro.domain

data class TodoItem (
    val id: Int,
    val text: String,
    val isDone: Boolean = false
)