package com.foxtask.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.foxtask.app.presentation.ui.components.FoxCharacter
import com.foxtask.app.presentation.ui.components.LevelUpAnimation
import com.foxtask.app.presentation.ui.components.XpProgressBar
import com.foxtask.app.presentation.ui.navigation.NavGraph
import com.foxtask.app.presentation.ui.navigation.Screen
import com.foxtask.app.presentation.ui.theme.FoxTaskTheme
import com.foxtask.app.presentation.viewmodel.MainViewModel
import com.foxtask.app.util.ErrorHandler
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FoxTaskTheme {
                MainScreenContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainViewModel: MainViewModel = viewModel()
    val mainUiState by mainViewModel.uiState.collectAsState()
    
    // Snackbar для отображения ошибок
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Подписка на ошибки из ErrorHandler
    LaunchedEffect(Unit) {
        ErrorHandler.errorFlow.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Обновлять itemsMap при каждом показе главного экрана
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mainViewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Главная") },
                    label = { Text("Лис") },
                    selected = currentRoute == Screen.Main.route,
                    onClick = { navController.navigate(Screen.Main.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Задачи") },
                    label = { Text("Задачи") },
                    selected = currentRoute == Screen.Tasks.route,
                    onClick = { navController.navigate(Screen.Tasks.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Магазин") },
                    label = { Text("Магазин") },
                    selected = currentRoute == Screen.Shop.route,
                    onClick = { navController.navigate(Screen.Shop.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Гардероб") },
                    label = { Text("Гардероб") },
                    selected = currentRoute == Screen.Wardrobe.route,
                    onClick = { navController.navigate(Screen.Wardrobe.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = "Статистика") },
                    label = { Text("Статистика") },
                    selected = currentRoute == Screen.Stats.route,
                    onClick = { navController.navigate(Screen.Stats.route) }
                )
            }
        },
        floatingActionButton = {
            if (currentRoute == Screen.Tasks.route) {
                FloatingActionButton(
                    onClick = { /* navigate to create task */ },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить задачу")
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// Упрощённый MainScreen встроен в NavGraph, но можно и здесь:
@Composable
fun MainScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTaskEdit: (Int?) -> Unit
) {
    val viewModel: MainViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar with stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Уровень ${state.level}",
                    style = MaterialTheme.typography.titleLarge
                )
                XpProgressBar(
                    currentXp = state.currentXp,
                    maxXp = state.totalXpForNextLevel
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.star_big_on),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.coins.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fox character
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FoxCharacter(
                outfit = state.outfit,
                level = state.level,
                itemsMap = state.itemsMap
            )
            LevelUpAnimation(
                show = state.showLevelUpAnimation,
                newLevel = state.newLevel,
                onAnimationComplete = {}
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick actions grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                icon = Icons.Filled.List,
                label = "Задачи",
                onClick = onNavigateToTasks
            )
            ActionButton(
                icon = Icons.Filled.ShoppingCart,
                label = "Магазин",
                onClick = onNavigateToShop
            )
            ActionButton(
                icon = Icons.Filled.Person,
                label = "Гардероб",
                onClick = onNavigateToWardrobe
            )
            ActionButton(
                icon = Icons.Filled.BarChart,
                label = "Статистика",
                onClick = onNavigateToStats
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
