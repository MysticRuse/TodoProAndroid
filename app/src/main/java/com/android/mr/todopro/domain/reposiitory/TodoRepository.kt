package com.android.mr.todopro.domain.reposiitory

import com.android.mr.todopro.domain.TodoItem
import kotlinx.coroutines.flow.Flow

// Interface for testability - easy to mock or swap.
interface TodoRepository {
    fun observeTodos(): Flow<List<TodoItem>>
    suspend fun addTodo(text: String)
    suspend fun toggleTodo(id: Int)
    suspend fun deleteTodo(id: Int)
    suspend fun syncWithServer(): Boolean
}