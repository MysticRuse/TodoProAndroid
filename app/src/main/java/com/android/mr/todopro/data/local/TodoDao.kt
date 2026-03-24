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

    @Query("SELECT * FROM todos ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TodoEntity)  // suspend = runs on background thread

    @Update
    suspend fun update(item: TodoEntity)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getById(id: Int): TodoEntity?
}