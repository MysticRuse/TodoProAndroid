# TodoProAndroid

## Phase 5 -- Hilt DI + Production Polish
Dependency Injection . Hilt . Module structure . Test

## Phase 4 -- WorkManager Background Sync
CoroutineWorker . Constraints . Periodic . One-Time . Sync-Status in UI

## Phase 3 -- Remote API with Retrofit
Retrofit . DTO . Mappers . networkResult . safeApiCall . Offline-First Flow

    - This phase demonstrates offline-first as a design constraint.
    - The app syncs with a remote server.
    - Local DB remains the source of truth.
    - Offline writes queue up and sync when connected.

### Steps:
    1. Retrofit dependencies in libs.version.toml, build.gradle.kts(app) and build.gradle.kts(Project)
    2. Add internet permission - <uses-permission android:name="android.permission.INTERNET"/>
    3. For a mock API, use JSONPlaceholder (https://jsonplaceholder.typicode.com/todos) — it's a free fake REST API with a /todos endpoint. No auth, no setup.
    4. Before networking code, update TodoEntity to track a Sync State.
        - @ColumnInfo(name = "sync_status") val syncStatus: String = "LOCAL_ONLY"
    5. Schema changed - Add Migration in TodoDatabase.
        - val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE todos ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'LOCAL_ONLY'"
                )
            }
        }
    6. Add a couple more DAOs for the syncStatus field
        - @Query("SELECT * FROM todos WHERE sync_status != 'SYNCED'")
          suspend fun getUnsyncedItems(): List<TodoEntity>
        - @Query("UPDATE todos SET sync_status = :status WHERE id = :id")
          suspend fun updateSyncStatus(id: Int, status: String)
    7. Add DTO and Api Interface - /data/remote/TodoDto.kt and /data/remote/TodoApiService.kt
    8. Create a NetworkClient object that has the Retrofit instance with OkHttpClient, HttpLoggingInterceptor and Json defined.
    9. Add a NetworkResult wrapper - network exceptions should not bubble to ViewModel raw - wrap every call.
        - sealed class NetworkResult<out T> {
            data class Success<T>(val data: T) : NetworkResult<T>()
            data class ApiError(val code: Int, val message: String) : NetworkResult<Nothing>()
            data object NetworkError : NetworkResult<Nothing>()  // no connectivity
            data object Loading : NetworkResult<Nothing>()
        }
        - Extension function — call this around every Retrofit call
        - suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> {
            return try {
                NetworkResult.Success(call())
            } catch (e: HttpException) {
                NetworkResult.ApiError(e.code(), e.message())
            } catch (e: IOException) {
                NetworkResult.NetworkError   // covers no internet, timeout, etc.
            } catch (e: Exception) {
                NetworkResult.ApiError(-1, e.message ?: "Unknown error")
            }
        }
    10. Add mappers for DTO - TodoMappers.kt
        - enum class SyncStatus { LOCAL_ONLY, PENDING_SYNC, SYNCED }
        - Add a SyncStatus to domain model
        - Update TodoMapper.ky
            - DTO → Entity (what comes from server goes straight to DB)
                - fun TodoDto.toEntity() = TodoEntity( ... = ... etc)
            - Domain → DTO (for sending to server)
                - fun TodoItem.toDto() = TodoDto( ... = ... etc)
            - Update existing mapper to include syncStatus
                - fun TodoEntity.toDomain() = TodoItem( ..., ...,  syncStatus = SyncStatus.valueOf(syncStatus))
    11. Update Repository with offline-first sync - core of Offline-First
        - Update the ViewModel factory to pass in the apiService in repository.
        - fun factory(context: Context): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    // Get the DB and the repository
                    val db = TodoDatabase.Companion.getInstance(context)
                    val repository = TodoRepositoryImpl(db.todoDao(), NetworkClient.apiService)
                    TodoViewModel(repository)
                }
            }
        }
    12. Add insertAll to DAO.
    13. Add a syncWithServer() function in Repository that gets the Todos from the server and also pushes any pending Todos.
    14. Trigger a sync on app start from the ViewModel's init block.
    This app now loads from server on first launch, works offline and syncs changes.

## Phase 2 -- Local persistence with ROOM
Entity . DAO . Database . Repository Pattern . Flow from DB
    
    - This phase demonstrates why repository pattern is important.
    - Local data layer added along with the repository pattern.
    - Same app, but Todos survive app kills.

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
        -  fun factory(context: Context): ViewModelProvider.Factory {
                return viewModelFactory {
                initializer {
                    // Get the DB and the repository
                    val db = TodoDatabase.Companion.getInstance(context)
                    val repository = TodoRepositoryImpl(db.todoDao())
                    TodoViewModel(repository)
                    // TodoViewModel()
                }
            }

## Phase 1 -- In-memory Todo App
Compose Basics . LazyColumn . State . ViewModel + StateFlow . Navigation

    - This phase demonstrates Compose's rendering model and unidirectional flow.
    - ViewModel preserves state - app survives configuration change like rotation etc.

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

    