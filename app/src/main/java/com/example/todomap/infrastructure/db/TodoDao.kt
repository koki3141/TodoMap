package com.example.todomap.infrastructure.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT id, title, done, sort FROM todos ORDER BY done ASC, sort ASC")
    fun observeAll(): Flow<List<TodoEntity>>

    @Query("UPDATE todos SET done = CASE done WHEN 0 THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun toggleDone(id: String)

    @Query("UPDATE todos SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: String, title: String)

    @Query("SELECT sort FROM todos WHERE id = :id LIMIT 1")
    suspend fun getSortById(id: String): Int?

    @Query("SELECT MAX(sort) FROM todos")
    suspend fun getMaxSort(): Int?

    @Query("UPDATE todos SET sort = sort + 1 WHERE sort > :targetSort")
    suspend fun bumpSortAfter(targetSort: Int)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: TodoEntity)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun delete(id: String)
}
