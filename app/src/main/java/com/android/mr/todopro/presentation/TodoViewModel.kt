package com.android.mr.todopro.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android.mr.todopro.data.TodoRepositoryImpl
import com.android.mr.todopro.data.local.TodoDatabase
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
        // Observe DB changes and push to UI state.
        // Collect from flow in a coroutine.
        viewModelScope.launch {
            repository.observeTodos().collect { items ->
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

        viewModelScope.launch {     // launch coroutine for the suspend function
            repository.addTodo(text)
            _uiState.update { state ->       // clear the input box after adding to repository.
                state.copy(inputText = "") }
        }
    }

    fun toggleItem(id: Int) {
        viewModelScope.launch {
            repository.toggleTodo(id)
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }

    // Factory needed because ViewModel has a non-default constructor
    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    // Get the DB and the repository
                    val db = TodoDatabase.getInstance(context)
                    val repository = TodoRepositoryImpl(db.todoDao())
                    TodoViewModel(repository)
                }
            }
        }
    }
}

//======================================================================================
// Phase 1 - In-memory View Model
//class TodoViewModel(): ViewModel() {
//    /**
//     * A single object describing everything the UI needs
//     */
//    data class TodoUiState(
//        val items: List<TodoItem> = emptyList(),
//        val inputText : String = ""
//    )
//
//    // MutableStateFlow is the internal mutable state - not exposed.
//    private val _uiState = MutableStateFlow(TodoUiState())
//
//    // Expose read-only StateFlow to the UI
//    val uiState = _uiState.asStateFlow()
//
//    fun onInputChange(text: String) {
//        _uiState.update { currentState ->
//            currentState.copy(inputText = text)
//        }
//    }
//    fun addItem() {
//        val text = _uiState.value.inputText.trim()
//        if (text.isEmpty()) return
//
//        val newItem = TodoItem(
//            id = System.currentTimeMillis().toInt(),
//            text = text
//        )
//        _uiState.update { state ->
//            state.copy(
//                items = state.items + newItem,
//                inputText = ""
//            )
//        }
//    }
//    fun toggleItem(id: Int) {
//        _uiState.update { state ->
//            state.copy(
//                items = state.items.map { item ->
//                    if (item.id == id) item.copy(isDone = !item.isDone) else item
//                }
//            )
//        }
//    }
//    fun deleteItem(id: Int) {
//        _uiState.update { state ->
//            state.copy(
//                items = state.items.filter { it.id != id }
//            )
//        }
//    }
//    companion object {
//        fun factory(context: Context): ViewModelProvider.Factory {
//            return viewModelFactory {
//                initializer {
//                    TodoViewModel()
//                }
//            }
//        }
//    }
//}