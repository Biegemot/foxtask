package com.foxtask.app.data.repository

import com.foxtask.app.data.local.dao.HabitProgressDao
import com.foxtask.app.data.local.dao.InventoryDao
import com.foxtask.app.data.local.dao.InventoryWithItem
import com.foxtask.app.data.local.entities.*
import com.foxtask.app.data.models.ItemCategory
import com.foxtask.app.data.models.ItemTier
import com.foxtask.app.domain.models.Statistics

import kotlinx.coroutines.flow.Flow

interface FoxTaskRepository {
    // User
    suspend fun getUser(): User?
    fun getUserStream(): Flow<User>
    suspend fun updateUser(level: Int, currentXp: Int, coins: Int)
    suspend fun addCoins(amount: Int)
    suspend fun setCurrentXp(xp: Int)
    suspend fun setLevelAndXp(level: Int, xp: Int)

    // Tasks
    suspend fun getAllTasks(): List<Task>
    suspend fun getAllHabits(): List<Task>
    suspend fun getTaskById(id: Int): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun setTaskCompleted(taskId: Int, completed: Boolean)

    // Habit Progress
    suspend fun getProgressForHabit(habitId: Int): List<HabitProgress>
    suspend fun getProgressForDate(habitId: Int, date: Long): HabitProgress?
    suspend fun insertProgress(progress: HabitProgress)
    suspend fun updateProgress(progress: HabitProgress)

    // Items
    suspend fun getAllActiveItems(): List<Item>
    suspend fun getItemsByCategory(category: ItemCategory): List<Item>
    suspend fun getItemById(id: Int): Item?
    suspend fun getItemsByTier(tier: ItemTier): List<Item>

    // Inventory
    suspend fun getInventoryForUser(userId: Int): List<Inventory>
    suspend fun getEquippedItems(userId: Int): List<Inventory>
    suspend fun getInventoryItem(userId: Int, itemId: Int): Inventory?
    suspend fun getInventoryWithItems(userId: Int): List<InventoryWithItem>
    suspend fun insertInventoryItem(inventory: Inventory)
    suspend fun updateInventoryItem(inventory: Inventory)
    suspend fun setEquipped(inventoryId: Int, equipped: Boolean)
    suspend fun unequipAllForUser(userId: Int)
    suspend fun equipItem(inventoryId: Int)
    suspend fun unequipItem(inventoryId: Int)

    // Outfit
    suspend fun getOutfit(userId: Int): Outfit?
    fun getOutfitStream(userId: Int): Flow<Outfit?>
    suspend fun insertOutfit(outfit: Outfit)
    suspend fun updateOutfit(outfit: Outfit)

    // Statistics
    suspend fun getStatistics(): Statistics
}
