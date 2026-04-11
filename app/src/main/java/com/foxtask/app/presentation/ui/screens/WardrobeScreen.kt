package com.foxtask.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.foxtask.app.data.local.entities.Inventory
import com.foxtask.app.data.local.entities.Item
import com.foxtask.app.data.models.ItemCategory
import com.foxtask.app.data.models.ItemTier
import com.foxtask.app.presentation.ui.components.ItemCard
import com.foxtask.app.presentation.viewmodel.WardrobeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    onNavigateBack: () -> Unit,
    viewModel: WardrobeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Обновлять при каждом показе экрана
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

     val categories = listOf(
         ItemCategory.HAT,
         ItemCategory.GLASSES,
         ItemCategory.MASK,
         ItemCategory.SCARF,
         ItemCategory.BANDANA,
         ItemCategory.CLOAK,
         ItemCategory.FUR_COLOR,
         ItemCategory.BACKGROUND,
         ItemCategory.MAORI_PATTERN
     )

     Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Гардероб") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category selector
             LazyRow(
                 contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                 horizontalArrangement = Arrangement.spacedBy(8.dp)
             ) {
                 items(categories) { category ->
                     FilterChip(
                         selected = state.selectedCategory == category,
                         onClick = {
                             viewModel.selectCategory(category)
                         },
                         label = { Text(getCategoryName(category)) }
                     )
                 }
             }

            Spacer(modifier = Modifier.height(8.dp))

            val categoryItems = state.categoryItems
            val equippedId = state.equippedItemId

            if (categoryItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("В этой категории нет предметов", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                items(categoryItems) { (inv, item) ->
                    ItemCard(
                        name = item.name,
                        description = item.description,
                        cost = item.cost,
                        tier = item.tier,
                        isOwned = true,
                        isEquipped = inv.id == equippedId,
                        onBuyClick = { },
                        onEquipClick = { viewModel.equipFromInventory(inv.id) },
                        itemDrawableResName = item.drawableResName
                    )
                }
                }
            }
        }
    }
}

private fun getCategoryName(category: ItemCategory): String {
    return when (category) {
        ItemCategory.HAT -> "Головные уборы"
        ItemCategory.GLASSES -> "Очки"
        ItemCategory.MASK -> "Маски"
        ItemCategory.SCARF -> "Шарфы"
        ItemCategory.BANDANA -> "Банданы"
        ItemCategory.CLOAK -> "Накидки"
        ItemCategory.FUR_COLOR -> "Цвет меха"
        ItemCategory.BACKGROUND -> "Фоны"
        ItemCategory.MAORI_PATTERN -> "Маори"
    }
}
