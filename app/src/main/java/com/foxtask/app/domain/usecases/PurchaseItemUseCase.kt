package com.foxtask.app.domain.usecases

import com.foxtask.app.data.local.entities.Inventory
import com.foxtask.app.data.repository.FoxTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PurchaseItemUseCase(
    private val repository: FoxTaskRepository
) {
    suspend operator fun invoke(userId: Int, itemId: Int): Boolean = withContext(Dispatchers.IO) {
        val user = repository.getUser() ?: return@withContext false
        val item = repository.getItemById(itemId) ?: return@withContext false

        // Проверяем, достаточно ли монет
        if (user.coins < item.cost) return@withContext false

        // Проверяем, не куплен ли уже
        val existing = repository.getInventoryItem(userId, itemId)
        if (existing != null) return@withContext false

        // Вычитаем монеты
        repository.addCoins(-item.cost)

        // Добавляем в инвентарь
        val inventory = Inventory(
            id = 0, // autoGenerate
            userId = userId,
            itemId = itemId,
            purchaseDate = System.currentTimeMillis(),
            isEquipped = false
        )
        repository.insertInventoryItem(inventory)

        true
    }
}
