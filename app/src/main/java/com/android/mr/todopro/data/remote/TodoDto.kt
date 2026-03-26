package com.android.mr.todopro.data.remote

import kotlinx.serialization.Serializable


@Serializable
data class TodoDto (
    val userId: Int,
    val id: Int,
    val title: String,    // JSON placeholder uses 'title' and not 'text'
    val completed: Boolean
)