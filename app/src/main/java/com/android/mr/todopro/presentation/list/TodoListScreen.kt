package com.android.mr.todopro.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.mr.todopro.domain.TodoItem
import com.android.mr.todopro.util.UserSession

/**
 * Scaffold + TopAppBar + FAB
 * Wrap TodoListScreen in a Scaffold.
 *
 */

val userId = UserSession.CURRENT_USER_ID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoViewModel,
    onItemClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // The following ensures that the UI "jumps" to show the user their newly created task immediately.
    //=========================================================================================
    // This object allows us to programmatically control the scroll position of the LazyColumn.
    // Add this as state in LazyColumn(state = listState, ...)
    val listState = rememberLazyListState()

    // Track previous size to only scroll when a new item is added.
    var previousSize by remember { mutableIntStateOf(uiState.items.size) }

    // LaunchedEffect triggers whenever uiState.items.size changes.
    LaunchedEffect(key1 = uiState.items.size) {
        if (uiState.items.size > previousSize) {
            listState.animateScrollToItem(0)
        }
        previousSize = uiState.items.size
    }
    //=========================================================================================

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Todos") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addItem() }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        // CRITICAL: always pass padding to your content
        // this stops content going behind the TopAppBar/FAB
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChange,
                label = { Text("New task...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.addItem() }
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                )
            )
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    TodoRow(
                        item = item,
                        onToggle = { viewModel.toggleItem(item.id) },
                        onDelete = { viewModel.deleteItem(item.id) },
                        onClick = { onItemClick(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TodoRow(item: TodoItem, onToggle: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Change from onToggle() to onClick()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isDone,
            onCheckedChange = { onToggle() }
            // Note: Checkbox's onCheckedChange does NOT bubble up to the row's clickable
            // because Checkbox handles the pointer event itself
        )
        Text(
            text = item.text,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            // Strike through completed items
            textDecoration = if (item.isDone) TextDecoration.LineThrough
            else TextDecoration.None,
            color = if (item.isDone) MaterialTheme.colorScheme.outline
            else MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = { onDelete() }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

// TodoListScreen with viewModel injected
@Composable
fun TodoListScreenPhase1Step3(
    viewModel: TodoViewModel = viewModel(),
    ) {

    // collectAsStateWithLifecycle is the modern, lifecycle-aware way
    // to observe StateFlow in Compose (replaces LiveData.observe{})
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Wrap TodoList in a Scaffold - standard Material 3 app structure.
    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // Input Row at the top
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::onInputChange,
                    label = { Text("Add a task") },
                    modifier = Modifier
                        .weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = viewModel::addItem) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LazyColumn
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.items, key = { it.id }) { item ->
                    TodoRowPhase1Step2Step3(
                        item = item,
                        onToggle = { viewModel.toggleItem(item.id) },
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TodoAppScreenPhase1Step2NoViewModel() {

    // Note: 'remember' keeps this alive across recompositions.
    // "mutableStateOf" makes Compose re-draw whenever the value changes.
    var items by remember {
        mutableStateOf(
            listOf(
                TodoItem(1, userId, "Buy groceries"),
                TodoItem(2, userId, "Read Compose Docs"),
                TodoItem(3, userId = 5, "Complete Kotlin Crash Course"),
            )
        )
    }

    var newItemText by remember { mutableStateOf("") }
    Scaffold() {
            innerPadding ->
        Column(modifier = Modifier.fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
        ) {

            // Input Row at the top
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    label = { Text("Add a task") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (newItemText.isNotBlank()) {
                        items = items + TodoItem(
                            id = items.size + 1,
                            userId = userId,
                            text = newItemText
                        )
                        newItemText = ""
                    }
                }) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LazyColumn
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.id }) { item ->
                    TodoRowPhase1Step2Step3(
                        item = item,
                        onToggle = {
                            items = items.map {
                                if (it.id == item.id) it.copy(isDone = !it.isDone) else it
                            }
                        },
                    ) {
                        items = items.filter { it.id != item.id }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoRowPhase1Step2Step3(item: TodoItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isDone,
            onCheckedChange = { onToggle() }
        )
        Text(
            text = item.text,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            // Strike through completed items
            textDecoration = if (item.isDone) TextDecoration.LineThrough
            else TextDecoration.None,
            color = if (item.isDone) MaterialTheme.colorScheme.outline
            else MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = { onDelete() }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
fun TodoRowPhase1Step1(item: TodoItem) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isDone,
            onCheckedChange = { /* Handle checkbox state change */ } // Nothing yet for now
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun TodoAppPhase1Step1() {
    // Note: Hardcoded - replace with ViewModel
    val items = listOf(
        TodoItem(1, userId, "Buy groceries"),
        TodoItem(2, userId, "Read Compose Docs"),
        TodoItem(3, userId, "Complete Kotlin Crash Course"),
    )

    // ListView of items with LazyColumn
    Scaffold (
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                TodoRowPhase1Step1(item)
            }
        }
    }

}

@Composable
fun TodoAppHelloWorld() {
    // Entire app lives here.
    // Note: Add the Scaffold with innerPadding so that Text does not overlap on the title bar.
    // Note: innerPadding: This variable (passed by the Scaffold) contains the exact pixel measurements of the system bars.
    Scaffold (
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Text(
            text = "My Todo App",
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        )
    }
}
