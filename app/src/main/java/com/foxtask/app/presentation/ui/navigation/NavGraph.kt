package com.foxtask.app.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.foxtask.app.MainScreen
import com.foxtask.app.presentation.ui.screens.ShopScreen
import com.foxtask.app.presentation.ui.screens.StatsScreen
import com.foxtask.app.presentation.ui.screens.TasksScreen
import com.foxtask.app.presentation.ui.screens.WardrobeScreen
import com.foxtask.app.presentation.ui.screens.TaskEditScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                onNavigateToShop = { navController.navigate(Screen.Shop.route) },
                onNavigateToWardrobe = { navController.navigate(Screen.Wardrobe.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToTaskEdit = { taskId -> navController.navigate(Screen.TaskEdit.createRoute(taskId)) }
            )
        }
        composable(Screen.Tasks.route) {
            TasksScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTaskEdit = { taskId -> navController.navigate(Screen.TaskEdit.createRoute(taskId)) }
            )
        }
        composable(Screen.Shop.route) {
            ShopScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Wardrobe.route) {
            WardrobeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.TaskEdit.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType; nullable = true }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            TaskEditScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
