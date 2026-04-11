package com.foxtask.app.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.foxtask.app.data.local.dao.InventoryDao
import com.foxtask.app.data.local.dao.InventoryWithItem
import com.foxtask.app.data.local.entities.Inventory
import com.foxtask.app.data.local.entities.Item
import com.foxtask.app.data.local.entities.Outfit
import com.foxtask.app.data.models.ItemCategory
import com.foxtask.app.data.repository.FoxTaskRepository
import com.foxtask.app.domain.usecases.EquipItemUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WardrobeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = com.foxtask.app.di.ServiceLocator.getRepository()
    private val equipItemUseCase = com.foxtask.app.di.ServiceLocator.getEquipItemUseCase()

    private val _uiState = MutableStateFlow(WardrobeUiState())
    val uiState: StateFlow<WardrobeUiState> = _uiState.asStateFlow()

    init {
        loadInventory()
        loadOutfit()
    }

    fun refresh() {
        loadInventory()
        loadOutfit()
    }

    fun loadInventory() {
        viewModelScope.launch {
            val inventoryWithItems = repository.getInventoryWithItems(1)
            _uiState.value = _uiState.value.copy(
                inventory = inventoryWithItems.map { it.inventory },
                inventoryWithItems = inventoryWithItems
            )
        }
    }

    fun loadOutfit() {
        viewModelScope.launch {
            val outfit = repository.getOutfit(1)
            _uiState.value = _uiState.value.copy(outfit = outfit)
        }
    }

    fun selectCategory(category: ItemCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun unequipCategory(category: ItemCategory) {
        viewModelScope.launch {
            // Убираем предмет из слота outfit
            val outfit = _uiState.value.outfit ?: return@launch
            val updated = when (category) {
                ItemCategory.HAT -> outfit.copy(hatItemId = null)
                ItemCategory.GLASSES -> outfit.copy(glassesItemId = null)
                ItemCategory.MASK -> outfit.copy(maskItemId = null)
                ItemCategory.SCARF -> outfit.copy(scarfItemId = null)
                ItemCategory.BANDANA -> outfit.copy(bandanaItemId = null)
                ItemCategory.CLOAK -> outfit.copy(cloakItemId = null)
                ItemCategory.FUR_COLOR -> outfit.copy(furColorItemId = null)
                ItemCategory.BACKGROUND -> outfit.copy(backgroundThemeId = null)
                ItemCategory.MAORI_PATTERN -> outfit.copy(maoriPatternItemId = null)
            }
            repository.updateOutfit(updated)
            loadOutfit()
        }
    }

    fun equipFromInventory(inventoryId: Int) {
        viewModelScope.launch {
            equipItemUseCase.invoke(1, inventoryId)
            loadInventory()
            loadOutfit()
        }
    }

    data class WardrobeUiState(
        val inventory: List<com.foxtask.app.data.local.entities.Inventory> = emptyList(),
        val inventoryWithItems: List<InventoryWithItem> = emptyList(),
        val outfit: Outfit? = null,
        val selectedCategory: ItemCategory = ItemCategory.HAT
    ) {
        val equippedItemId: Int?
            get() = when (selectedCategory) {
                ItemCategory.HAT -> outfit?.hatItemId
                ItemCategory.GLASSES -> outfit?.glassesItemId
                ItemCategory.MASK -> outfit?.maskItemId
                ItemCategory.SCARF -> outfit?.scarfItemId
                ItemCategory.BANDANA -> outfit?.bandanaItemId
                ItemCategory.CLOAK -> outfit?.cloakItemId
                ItemCategory.FUR_COLOR -> outfit?.furColorItemId
                ItemCategory.BACKGROUND -> outfit?.backgroundThemeId
                ItemCategory.MAORI_PATTERN -> outfit?.maoriPatternItemId
            }

        val categoryItems: List<InventoryWithItem>
            get() = inventoryWithItems.filter { (_, item) ->
                item.category == selectedCategory
            }
    }
}
