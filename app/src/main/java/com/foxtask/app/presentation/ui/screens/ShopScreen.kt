package com.foxtask.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.foxtask.app.data.models.ItemCategory
import com.foxtask.app.presentation.ui.components.ItemCard
import com.foxtask.app.presentation.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onNavigateBack: () -> Unit,
    viewModel: ShopViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Обновлять при каждом показе
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
         ItemCategory.SCARF,
         ItemCategory.CLOAK,
         ItemCategory.FUR_COLOR,
         ItemCategory.BACKGROUND,
         ItemCategory.MAORI_PATTERN
     )

     Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Магазин") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.star_big_on),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = state.coins.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
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
            // Category chips
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

            // Items grid
            val filtered = state.filteredItems
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Предметов в этой категории нет", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(180.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    gridItems(filtered) { item ->
                        val isOwned = state.inventory.any { it.itemId == item.id }
                        val isEquipped = state.inventory.any { it.itemId == item.id && it.isEquipped }

                        ItemCard(
                            name = item.name,
                            description = item.description,
                            cost = item.cost,
                            tier = item.tier,
                            isOwned = isOwned,
                            isEquipped = isEquipped,
                            onBuyClick = {
                                viewModel.purchaseItem(item.id)
                            },
                            onEquipClick = {
                                val inv = state.inventory.find { it.itemId == item.id }
                                inv?.let { viewModel.equipItem(it.id) }
                            },
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
