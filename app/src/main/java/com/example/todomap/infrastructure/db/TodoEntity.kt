package com.example.todomap.infrastructure.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.todomap.domain.TodoItem

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "done") val done: Boolean,
    @ColumnInfo(name = "sort") val sort: Int,
)

fun TodoEntity.toModel(): TodoItem = TodoItem(
    id = id,
    title = title,
    done = done,
)

