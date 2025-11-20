package com.example.todomap.ui

import com.example.todomap.domain.TodoItem

data class TodoUiState(
    val todoItems: List<TodoItem> = emptyList(),
    val selectedTodoItemId: String? = null,
    val scrollRequestNonce: String? = null,
    val inputTodoItemText: String = "",
    val showCompleted: Boolean = false,
)
