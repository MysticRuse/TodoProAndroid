# TodoProAndroid

## Phase 1 - In-memory Todo App
Compose Basics . LazyColumn . State . ViewModel + StateFlow . Navigation

### Steps:
    - 1. First Composable function in MainActivity displaying a Text.
    - 2. Display hardcoded list in a LazyColumn with 3 layout primitives in 'TodoRow' Composable 
        - The data model ('TodoItem.kt') + hardcoded list
        - Row of the LazyColum shows CheckBox, Spacer and Text
    - 3. Now add State + InterActivity ('remember' and 'mutableStateOf' - understand these to understand Compose's rendering model)
        - Modify the TodoAppScreen wiith OutLinedTextField to 'Add a task' alomg with Button to 'Add' the task
        - Update Row in LazyColumn with Delete Button along with the Checkbox.
        - Add 'onToggle' and 'onDelete' handling
        - NOW - we have a fully interactive in-memory todo list.
            - Add items, toggle, delete.
            - Rotate the device — notice state is lost - in comes ViewModel next to preserve state.
    - 4. Extract to ViewModel
            - Define ViewModel class in presentation layer
            - Define TodoUiState data class inside it with list of items and inputText
            - TodoViewModel : ViewModel()
                - Define mutableStateFlow of TodoUiState
                - Define read-only StateFlow of TodoUiState
                - Implement onInputChange() to update the inputText
                - Implement addItem() to add a new item
                - Implement toggleItem() to toggle the isDone state of an item
                - Implement deleteItem() to remove an item
    - 5. Update TodoAppScreen() to use the above ViewModel
            - Get the uiState from ViewModel using the collectAsStateWithLifeCycle.
            - Replace inplace item updates with calling viewModel methods
            - NOW - we have state-preserving TodoAppScreen - where state survives on device rotation - ViewModel doing its job.
    - 6: Add navigation to a detail screen
    - 7. Add a detail screen using the same viewModel.

    