package com.example.todomap.domain

data class TodoItem(
    val id: String,
    val title: String,
    val done: Boolean = false,
)