package com.android.mr.todopro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.mr.todopro.presentation.detail.TodoDetailScreen
import com.android.mr.todopro.presentation.list.TodoListScreen
import com.android.mr.todopro.presentation.list.TodoViewModel
import com.android.mr.todopro.ui.theme.TodoProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Note: setContent replaces setContentView(R.layout.*)
            // Everything inside the setContent block is a Composable function
            // Composable functions are the "views"
            TodoProTheme {
                // Initially - Phase 1 Step 1-3 - call TodoListScreen() directly
                // TodoListScreen()

                // Afterward - Add navigation
                // Create View Model ONCE here - scoped to Activity.
                // Both screens will receive same instance
                val viewModel: TodoViewModel = viewModel(
                     factory = TodoViewModel.factory(LocalContext.current)
                )

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "todo_list") {
                    composable(route = "todo_list") {
                        TodoListScreen(
                            viewModel = viewModel,
                            onItemClick = { itemId ->
                                navController.navigate("todo_detail/$itemId")
                            }
                        )
                    }
                    composable(
                        route = "todo_detail/{itemId}",
                        arguments = listOf(
                            navArgument("itemId") { type = NavType.IntType }
                        )
                    ) {
                        TodoDetailScreen(
                            viewModel = viewModel,
                            itemId = it.arguments?.getInt("itemId") ?: 0,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TodoProTheme {
        Greeting("Android")
    }
}