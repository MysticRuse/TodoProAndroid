package com.android.mr.todopro.presentation

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