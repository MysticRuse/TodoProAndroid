# TodoProAndroid

## Phase 5 -- Hilt DI + Production Polish

## Phase 4 -- WorkManager Background Sync
COroutineWorker . Constraints . Periodic . One-Time . Sync-Status in UI

## Phase 3 -- Remote API with Retrofit
Retrofit . DTO . Mappers . networkResult . safeApiCall . Offline-First Flow

## Phase 2 -- Local persistence with ROOM
Entity . DAO . Database . Repository Pattern . Flow from DB

    - Same app, but Todos survive app kills.
    - Understand full local data layer and the repository pattern

### Steps:
    - 1. ROOM dependencies in libs.version.toml, build.gradle.kts(app) and build.gradle.kts(Project)
    - 2. ROOM Entity - in data layer. It is a DB row. /data/local/TodoEntity.kt
    - 3. Data Access Object interface - DAO (has the DB commands and queries) /data/local/TodoDao.kt
    - 4. Database class (abstract class) to define the database instance and provide the DAO.  /data/local/TodoDatabase.kt
    - 5. Mapper Functions - Entity -> Domain mapppers and vice-versa /data/mappers.kt DB row becomes the busniess object
    - 6. Repository - !!IMP!!
            - ViewModel talks only to repository - not Room or any future API directly.
            - TodoRepository Interface in the domain layer.
                - Observe Todos and return a Flow with the list of the Todos whenever there is any change.
                - Add a todo (suspend fun)
                - Update a todo (suspend fun)
                - Delete a todo (suspend fun)
            - TodoRepository Implementation in the data layer.
                - It takes in a Dao object to call into the Room database.
    - 7. ViewModel to now use the TodoRepository interface instead of updating in-memory uiState directly.
    - 8. Update the ViewModel factory to pass in the repository with Dao.
            - initializer {
                    // Get the DB and the repository
                    val db = TodoDatabase.getInstance(context)
                    val repository = TodoRepositoryImpl(db.todoDao())
                    TodoViewModel(repository)
                    // TodoViewModel()
              }

## Phase 1 -- In-memory Todo App
Compose Basics . LazyColumn . State . ViewModel + StateFlow . Navigation

    - ViewModel preserves state - app survives configuration chnage like rotation etc.

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
            - Define a ViewModel::factory() to - same VM should be used by both main screen and detail screen.
                - companion object {
                    fun factory(context: Context): ViewModelProvider.Factory {
                        return viewModelFactory {
                            initializer {
                                TodoViewModel()
                            }
                        }
                    }
                }
    - 5. Update TodoAppScreen() to use the above ViewModel
            - Get the uiState from ViewModel using the collectAsStateWithLifeCycle.
            - Replace inplace item updates with calling viewModel methods
            - NOW - we have state-preserving TodoAppScreen - where state survives on device rotation - ViewModel doing its job.
    - 6: Add navigation to a detail screen
            - Create a Todo detail screen in presentation layer using the same ViewModel
            - Wire up navigation in MainActivity
    - 7. Scaffold + TopAppBar + FAB
            - Wrap TodoListScreen in a Scaffold - standard Material 3 app structure.
            - Add onClick to FAB(Floating Action Button) instead of a a "Add" button as was present as a Row item in Textbox Row.

    