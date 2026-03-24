package com.android.mr.todopro.data

import com.android.mr.todopro.data.local.TodoDao
import com.android.mr.todopro.data.local.TodoEntity
import com.android.mr.todopro.data.mappers.toDomain
import com.android.mr.todopro.domain.TodoItem
import com.android.mr.todopro.domain.reposiitory.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The repository implementation needs to take in the data access object (DAO)
 *  - to be able to access the database for observing, adding, deleting etc.
 */
class TodoRepositoryImpl(
    private val todoDao: TodoDao
): TodoRepository {
    override fun observeTodos(): Flow<List<TodoItem>> {
        return todoDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTodo(text: String) {
        todoDao.insert(
            TodoEntity(
                id = System.currentTimeMillis().toInt(),
                text = text,
                isDone = false
            )
        )
    }

    override suspend fun toggleTodo(id: Int) {
        val entity = todoDao.getById(id) ?: return
        todoDao.update(entity.copy(isDone = !entity.isDone))
    }

    override suspend fun deleteTodo(id: Int) {
        todoDao.deleteById(id)
    }
}