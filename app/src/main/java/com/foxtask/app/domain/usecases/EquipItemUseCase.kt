package com.foxtask.app.domain.usecases

import com.foxtask.app.data.local.entities.Inventory
import com.foxtask.app.data.models.ItemCategory
import com.foxtask.app.data.repository.FoxTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EquipItemUseCase(
    private val repository: FoxTaskRepository
) {
    suspend operator fun invoke(userId: Int, inventoryId: Int): Boolean = withContext(Dispatchers.IO) {
        val inventory = repository.getInventoryItem(userId, inventoryId) ?: return@withContext false
        val item = repository.getItemById(inventory.itemId) ?: return@withContext false

        // Если предмет уже надеван, снимаем
        if (inventory.isEquipped) {
            repository.setEquipped(inventoryId, false)
            // Обновляем Outfit: снять предмет из соответствующего слота
            val outfit = repository.getOutfit(userId) ?: com.foxtask.app.data.local.entities.Outfit(userId = userId)
            val updatedOutfit = clearOutfitSlot(outfit, item.category)
            repository.updateOutfit(updatedOutfit)
            return@withContext true
        }

        // Проверяем, не совмещается ли с другим предметом той же категории
        val equippedItems = repository.getEquippedItems(userId)
        val conflict = equippedItems.find { equippedInv ->
            val equippedItem = repository.getItemById(equippedInv.itemId)
            equippedItem?.category == item.category
        }

        // Если есть конфликт, снимаем старый
        conflict?.let { oldInv ->
            repository.setEquipped(oldInv.id, false)
            val oldItem = repository.getItemById(oldInv.itemId)
            oldItem?.let {
                val outfit = repository.getOutfit(userId) ?: com.foxtask.app.data.local.entities.Outfit(userId = userId)
                val updatedOutfit = clearOutfitSlot(outfit, it.category)
                repository.updateOutfit(updatedOutfit)
            }
        }

        // Надеваем новый
        repository.setEquipped(inventoryId, true)

        // Обновляем Outfit
        val outfit = repository.getOutfit(userId) ?: com.foxtask.app.data.local.entities.Outfit(userId = userId)
        val updatedOutfit = setOutfitSlot(outfit, item.category, item.id)
        repository.updateOutfit(updatedOutfit)

        true
    }

    private fun clearOutfitSlot(outfit: com.foxtask.app.data.local.entities.Outfit, category: ItemCategory): com.foxtask.app.data.local.entities.Outfit {
        return when (category) {
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
    }

    private fun setOutfitSlot(outfit: com.foxtask.app.data.local.entities.Outfit, category: ItemCategory, itemId: Int): com.foxtask.app.data.local.entities.Outfit {
        return when (category) {
            ItemCategory.HAT -> outfit.copy(hatItemId = itemId)
            ItemCategory.GLASSES -> outfit.copy(glassesItemId = itemId)
            ItemCategory.MASK -> outfit.copy(maskItemId = itemId)
            ItemCategory.SCARF -> outfit.copy(scarfItemId = itemId)
            ItemCategory.BANDANA -> outfit.copy(bandanaItemId = itemId)
            ItemCategory.CLOAK -> outfit.copy(cloakItemId = itemId)
            ItemCategory.FUR_COLOR -> outfit.copy(furColorItemId = itemId)
            ItemCategory.BACKGROUND -> outfit.copy(backgroundThemeId = itemId)
            ItemCategory.MAORI_PATTERN -> outfit.copy(maoriPatternItemId = itemId)
        }
    }
}

