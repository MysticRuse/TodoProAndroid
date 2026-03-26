package com.android.mr.todopro.presentation.list

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android.mr.todopro.data.local.TodoDatabase
import com.android.mr.todopro.data.remote.NetworkClient
import com.android.mr.todopro.data.repository.TodoRepositoryImpl
import com.android.mr.todopro.domain.TodoItem
import com.android.mr.todopro.domain.reposiitory.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Phase 1: In-Memory Todo App
 * Phase 2: Local persistence with ROOM
 *  * Pass in the repository to the ViewModel
 * Phase 3: Remote API with Retrofit
 */
class TodoViewModel(
    private val repository: TodoRepository
): ViewModel() {

    /**
     * A single object describing everything the UI needs
     */
    data class TodoUiState(
        val items: List<TodoItem> = emptyList(),
        val inputText : String = ""
    )

    // MutableStateFlow is the internal mutable state - not exposed.
    private val _uiState = MutableStateFlow(TodoUiState())

    // Expose read-only StateFlow to the UI
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "Initializing ViewModel")

        // The app loads from server on first launch, works offline, and syncs changes.
        viewModelScope.launch {
            Log.d(TAG, "Starting initial sync...")
            val success = repository.syncWithServer()
            Log.d(TAG, "Initial sync completed. Success: $success")
        }

        // Observe DB changes and push to UI state.
        // Collect from flow in a coroutine.
        viewModelScope.launch {
            repository.observeTodos().collect { items ->
                Log.d(TAG, "Received ${items.size} items from repository")
                _uiState.update { currentState ->
                    currentState.copy(items = items)
                }
            }
        }
    }
    fun onInputChange(text: String) {
        _uiState.update { currentState ->
            currentState.copy(inputText = text)
        }
    }

    fun addItem() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        Log.d(TAG, "Adding item: $text")
        viewModelScope.launch {     // launch coroutine for the suspend function
            repository.addTodo(text)
            _uiState.update { state ->       // clear the input box after adding to repository.
                state.copy(inputText = "") }
        }
    }

    fun toggleItem(id: Int) {
        Log.d(TAG, "Toggling item: $id")
        viewModelScope.launch {
            repository.toggleTodo(id)
        }
    }

    fun deleteItem(id: Int) {
        Log.d(TAG, "Deleting item: $id")
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }

    // Factory needed because ViewModel has a non-default constructor
    companion object {
        private const val TAG = "TodoViewModel"

        fun factory(context: Context): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    // Get the DB and the repository
                    val db = TodoDatabase.Companion.getInstance(context)
                    val repository = TodoRepositoryImpl(db.todoDao(), NetworkClient.apiService)
                    TodoViewModel(repository)
                }
            }
        }
    }
}
