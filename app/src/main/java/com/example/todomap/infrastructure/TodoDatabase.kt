package com.example.todomap.infrastructure

import android.content.Context
import androidx.room.withTransaction
import com.example.todomap.domain.TodoItem
import com.example.todomap.domain.TodoRepository
import com.example.todomap.infrastructure.db.TodoDao
import com.example.todomap.infrastructure.db.TodoEntity
import com.example.todomap.infrastructure.db.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class TodoDatabase(
    private val db: AppDatabase,
    private val dao: TodoDao = db.todoDao(),
) : TodoRepository {

    override fun observe(): Flow<List<TodoItem>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun switchTodoItemStatus(id: String) = withContext(Dispatchers.IO) { dao.toggleDone(id) }

    override suspend fun setTodoItemText(id: String, title: String) = withContext(Dispatchers.IO) { dao.updateTitle(id, title) }

    override suspend fun insertNewTodoItemAfter(id: String, title: String): String = withContext(Dispatchers.IO) {
        val newId = UUID.randomUUID().toString()
        db.withTransaction {
            val targetSort = run {
                val byId = dao.getSortById(id)
                val max = dao.getMaxSort()
                byId ?: (max ?: -1)
            }
            dao.bumpSortAfter(targetSort)
            dao.insert(
                TodoEntity(
                    id = newId,
                    title = title,
                    done = false,
                    sort = targetSort + 1,
                )
            )
        }
        newId
    }

    companion object {
        fun provide(context: Context): TodoDatabase =
            TodoDatabase(AppDatabase.build(context))
    }
}
