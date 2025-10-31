package com.example.todomap.domain

import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observe(): Flow<List<TodoItem>>
    suspend fun switchTodoItemStatus(id: String)
    suspend fun setTodoItemText(id: String, title: String)
    suspend fun insertNewTodoItemAfter(id: String, title: String = ""): String
}
