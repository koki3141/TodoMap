package com.example.todomap.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todomap.domain.TodoItem
import com.example.todomap.domain.TodoRepository
import com.example.todomap.infrastructure.TodoDatabase
import com.example.todomap.ui.theme.TodoMapTheme

@Composable
fun TodoRoute() {
    val context = LocalContext.current
    val repository = remember { TodoDatabase.provide(context) }
    val todViewModel: TodoViewModel = viewModel(factory = TodoViewModelFactory.create(repository))
    val state by todViewModel.uiState.collectAsStateWithLifecycle()

    TodoScreen(
        state = state,
        vm = todViewModel,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun TodoScreen(
    state: TodoUiState,
    vm: TodoViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 12.dp, top = 20.dp, end = 12.dp, bottom = 12.dp)
    ) {
        SectionHeader(title = "未完了", trailing = null)
        TodoList(
            items = state.todoItems.filter { !it.done },
            selectedId = state.selectedTodoItemId,
            onToggle = { vm.switchTodoItemStatus(it) },
            onSelect = { id -> vm.selectTodoItem(id) },
            onUpdateTitle = { id, text -> vm.setTodoItemText(id, text) },
            onInsertAfter = { vm.insertNewTodoItemAfter(it) },
            onDelete = { vm.deleteTodoItem(it) },
            modifier = Modifier.fillMaxWidth(),
            footer = {
                AddNewFooterRow(
                    onClick = {
                        vm.insertNewTodoItemAfter("")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.clickable { vm.setShowCompleted(!state.showCompleted) }
        ) {
            SectionHeader(
                title = "完了",
                trailing = {
                    val rotation by animateFloatAsState(
                        targetValue = if (state.showCompleted) 0f else 180f,
                        label = "chevronRotation"
                    )
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = if (state.showCompleted) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            )
        }

        AnimatedVisibility(
            visible = state.showCompleted,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            TodoList(
                items = state.todoItems.filter { it.done },
                selectedId = state.selectedTodoItemId,
                onToggle = { vm.switchTodoItemStatus(it) },
                onSelect = { id -> vm.selectTodoItem(id) },
                onUpdateTitle = { id, text -> vm.setTodoItemText(id, text) },
                onInsertAfter = { vm.insertNewTodoItemAfter(it) },
                onDelete = { vm.deleteTodoItem(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailing: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        if (trailing != null) {
            trailing()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun TodoList(
    items: List<TodoItem>,
    selectedId: String?,
    onToggle: (String) -> Unit,
    onSelect: (String) -> Unit,
    onUpdateTitle: (String, String) -> Unit,
    onInsertAfter: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
) {
    LazyColumn(modifier = modifier) {
        items(items, key = { it.id }) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = item.done, onCheckedChange = { onToggle(item.id) })
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(item.id) }
                ) {
                    var title by remember(item.id, item.title) { mutableStateOf(item.title) }
                    val focusRequester = remember { FocusRequester() }
                    val keyboard = LocalSoftwareKeyboardController.current
                    BasicTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            onUpdateTitle(item.id, it)
                        },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { onInsertAfter(item.id) }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                    LaunchedEffect(selectedId) {
                        if (selectedId == item.id) {
                            focusRequester.requestFocus()
                            keyboard?.show()
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onDelete(item.id) }
                )
            }
        }
        footer?.let {
            item { footer() }
        }
    }
}

@Composable
private fun AddNewFooterRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add",
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "新規タスクを追加",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun previewVm(): TodoViewModel {
    val repo = object : TodoRepository {
        private val list: SnapshotStateList<TodoItem> = mutableStateListOf(
            TodoItem("1", "Preview 1", false),
            TodoItem("2", "Preview 2", true),
        )

        override fun observe() = snapshotFlow { list.toList() }
        override suspend fun switchTodoItemStatus(id: String) {}
        override suspend fun setTodoItemText(id: String, title: String) {}
        override suspend fun insertNewTodoItemAfter(id: String, title: String): String = "new"
        override suspend fun delete(id: String) {}
    }
    return TodoViewModel(repository = repo)
}

@Preview(showBackground = true)
@Composable
private fun TodoScreenPreview() {
    TodoMapTheme {
        TodoScreen(
            state = TodoUiState(
                todoItems = listOf(
                    TodoItem(
                        id = "1",
                        title = "買い物する",
                        done = false,
                    ),
                    TodoItem(
                        id = "2",
                        title = "資料作成",
                        done = false,
                    ),
                    TodoItem(
                        id = "3",
                        title = "画像追加のテスト",
                        done = true,
                    ),
                ),
                inputTodoItemText = "新しいタスク",
                showCompleted = true,
                selectedTodoItemId = "1",
            ),
            vm = previewVm()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoEmptyScreenPreview() {
    TodoMapTheme {
        TodoScreen(
            state = TodoUiState(
                todoItems = listOf(
                ),
                inputTodoItemText = "新しいタスク",
                showCompleted = true,
                selectedTodoItemId = "1",
            ),
            vm = previewVm()
        )
    }
}

