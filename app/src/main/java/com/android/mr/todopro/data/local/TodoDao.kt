package com.android.mr.todopro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    // Flow means Room will re-emit everytime the table changes.
    // UI auto-updates with zero-polling - key Compose + Room superpower

    // Always filter by userId — other users' data never leaks into the UI
    @Query("SELECT * FROM todos WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeAll(userId: Int): Flow<List<TodoEntity>>
    @Query("SELECT * FROM todos WHERE sync_status != 'SYNCED' AND user_id = :userId")
    suspend fun getUnsyncedItems(userId: Int): List<TodoEntity>

    @Query("UPDATE todos SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TodoEntity)  // suspend = runs on background thread

    @Insert(onConflict = OnConflictStrategy.IGNORE) // IGNORE = don't overwrite local changes.
    suspend fun insertAll(items: List<TodoEntity>)

    @Update
    suspend fun update(item: TodoEntity)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: Int): TodoEntity?

}