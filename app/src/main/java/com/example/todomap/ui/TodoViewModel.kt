package com.example.todomap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todomap.domain.TodoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TodoViewModel(
    private val repository: TodoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    private fun setState(reducer: TodoUiState.() -> TodoUiState) {
        _uiState.value = _uiState.value.reducer()
    }

    private var observeJob: Job? = null

    init {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observe().collectLatest { list ->
                setState { copy(todoItems = list) }
            }
        }
    }

    fun switchTodoItemStatus(id: String) = viewModelScope.launch { repository.switchTodoItemStatus(id) }

    fun setTodoItemText(id: String, text: String) =
        viewModelScope.launch { repository.setTodoItemText(id, text) }

    fun setShowCompleted(show: Boolean) {
        setState { copy(showCompleted = show) }
    }

    fun selectTodoItem(id: String) {
        setState { copy(selectedTodoItemId = id) }
    }

    fun insertNewTodoItemAfter(id: String) {
        viewModelScope.launch {
            val newId = repository.insertNewTodoItemAfter(id)
            setState { copy(selectedTodoItemId = newId) }
        }
    }

    fun deleteTodoItem(id: String) {
        viewModelScope.launch {
            val currentList = uiState.value.todoItems
            val deleting = currentList.firstOrNull { it.id == id }
            val nextId = if (deleting != null && !deleting.done) {
                val incomplete = currentList.filter { !it.done }
                val pos = incomplete.indexOfFirst { it.id == id }
                val nextIncomplete = incomplete.getOrNull(pos + 1)?.id
                val prevIncomplete = incomplete.getOrNull(pos - 1)?.id
                nextIncomplete ?: prevIncomplete
            } else {
                val idx = currentList.indexOfFirst { it.id == id }
                if (idx >= 0) currentList.getOrNull(idx + 1)?.id ?: currentList.getOrNull(idx - 1)?.id else null
            }

            repository.delete(id)
            setState { copy(selectedTodoItemId = nextId) }
        }
    }
}

object TodoViewModelFactory {
    fun create(repository: TodoRepository): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TodoViewModel(repository) as T
            }
        }
}
