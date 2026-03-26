package com.android.mr.todopro.data.repository

import android.util.Log
import com.android.mr.todopro.data.local.TodoDao
import com.android.mr.todopro.data.local.TodoEntity
import com.android.mr.todopro.data.mappers.toDomain
import com.android.mr.todopro.data.mappers.toDto
import com.android.mr.todopro.data.mappers.toEntity
import com.android.mr.todopro.data.remote.NetworkResult
import com.android.mr.todopro.data.remote.TodoApiService
import com.android.mr.todopro.data.remote.safeApiCall
import com.android.mr.todopro.domain.SyncStatus
import com.android.mr.todopro.domain.TodoItem
import com.android.mr.todopro.domain.reposiitory.TodoRepository
import com.android.mr.todopro.util.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The repository implementation needs to take in the data access object (DAO)
 *  - to be able to access the database for observing, adding, deleting etc.
 */
class TodoRepositoryImpl(
    private val todoDao: TodoDao,
    private val todoApi: TodoApiService
): TodoRepository {

    companion object {
        private const val TAG = "TodoRepo"
    }

    private val userId = UserSession.CURRENT_USER_ID

    override fun observeTodos(): Flow<List<TodoItem>> {
        return todoDao.observeAll(userId).map { entities ->
            Log.d(TAG, "Observing todos, count: ${entities.size}")
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTodo(text: String) {
        // Write to DB instantly so UI updates immediately.
        val entity = TodoEntity(
            id = System.currentTimeMillis().toInt(),
            userId = userId,
            text = text,
            isDone = false,
            syncStatus = SyncStatus.PENDING_SYNC.name
        )
        Log.d(TAG, "Adding todo to local DB: $text")
        todoDao.insert(entity)

        // Try to sync right away - if it fails, WorkManager will retry later.
        syncSingleItem(entity)
    }

    override suspend fun toggleTodo(id: Int) {
        val entity = todoDao.getById(id) ?: return
        val newStatus = !entity.isDone
        Log.d(TAG, "Toggling todo $id to $newStatus in local DB")
        todoDao.update(entity.copy(isDone = newStatus, syncStatus = SyncStatus.PENDING_SYNC.name))

        syncSingleItem(entity)
    }

    override suspend fun deleteTodo(id: Int) {
        Log.d(TAG, "Deleting todo $id from local DB")
        todoDao.deleteById(id)

        // Fire and forget - it if fails, item is already gone locally.
        Log.d(TAG, "Deleting todo $id from server")
        val result = safeApiCall { todoApi.deleteTodo(id) }
        Log.d(TAG, "Delete from server result: $result")
    }

    override suspend fun syncWithServer() : Boolean {
        Log.d(TAG, "Starting full sync with server...")
        val result = safeApiCall { todoApi.getTodos(userId = userId) }
        if (result is NetworkResult.Success) {
            Log.d(TAG, "Fetched ${result.data.size} todos from server")
            // Take only first 20 to keep it manageable.
            //val remoteEntities = result.data.take(20).map { it.toEntity() }
            val remoteEntities = result.data.map { it.toEntity() }
            todoDao.insertAll(remoteEntities)
        } else {
            Log.e(TAG, "Failed to fetch todos from server: $result")
        }

        // Push any pending changes up to the server.
        val pending = todoDao.getUnsyncedItems(userId)
        Log.d(TAG, "Found ${pending.size} pending items to sync")
        pending.forEach { entity ->
            val pushResult = safeApiCall {
                todoApi.createTodo(entity.toDomain().toDto())
            }

            if (pushResult is NetworkResult.Success) {
                Log.d(TAG, "Successfully synced pending item: ${entity.id}")
                todoDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
            } else {
                Log.e(TAG, "Failed to sync pending item ${entity.id}: $pushResult")
            }
        }

        return result is NetworkResult.Success

    }

    private suspend fun syncSingleItem(entity: TodoEntity) {
        Log.d(TAG, "Syncing single item ${entity.id} to server...")
        val result = safeApiCall { todoApi.createTodo(entity.toDomain().toDto()) }
        if (result is NetworkResult.Success) {
            Log.d(TAG, "Successfully synced item ${entity.id}")
            todoDao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)
        } else {
            Log.e(TAG, "Failed to sync item ${entity.id}: $result")
        }
    }
}
