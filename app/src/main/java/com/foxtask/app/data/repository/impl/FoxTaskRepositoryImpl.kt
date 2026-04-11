package com.foxtask.app.data.repository.impl

import com.foxtask.app.data.local.dao.*
import com.foxtask.app.data.local.entities.*
import com.foxtask.app.data.models.*
import com.foxtask.app.data.repository.FoxTaskRepository
import com.foxtask.app.domain.models.Statistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FoxTaskRepositoryImpl(
    private val userDao: UserDao,
    private val taskDao: TaskDao,
    private val habitProgressDao: HabitProgressDao,
    private val itemDao: ItemDao,
    private val inventoryDao: InventoryDao,
    private val outfitDao: OutfitDao
) : FoxTaskRepository {

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L
        private const val MILLIS_PER_HOUR = 3_600_000L
        private const val MILLIS_PER_MINUTE = 60_000L
    }

    // User
    override suspend fun getUser(): User? = userDao.getUser()
    override fun getUserStream(): Flow<User> = userDao.getUserStream()
    override suspend fun updateUser(level: Int, currentXp: Int, coins: Int) {
        userDao.updateUser(level, currentXp, coins)
    }
    override suspend fun addCoins(amount: Int) = userDao.addCoins(amount)
    override suspend fun setCurrentXp(xp: Int) = userDao.setCurrentXp(xp)
    override suspend fun setLevelAndXp(level: Int, xp: Int) = userDao.setLevelAndXp(level, xp)

    // Tasks
    override suspend fun getAllTasks(): List<Task> = taskDao.getAllTasks()
    override suspend fun getAllHabits(): List<Task> = taskDao.getAllHabits()
    override suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)
    override suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    override suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    override suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    override suspend fun setTaskCompleted(taskId: Int, completed: Boolean) =
        taskDao.setTaskCompleted(taskId, completed)

    // Habit Progress
    override suspend fun getProgressForHabit(habitId: Int): List<HabitProgress> =
        habitProgressDao.getProgressForHabit(habitId)
    override suspend fun getProgressForDate(habitId: Int, date: Long): HabitProgress? =
        habitProgressDao.getProgressForDate(habitId, date)
    override suspend fun insertProgress(progress: HabitProgress) =
        habitProgressDao.insertProgress(progress)
    override suspend fun updateProgress(progress: HabitProgress) =
        habitProgressDao.updateProgress(progress)

    // Items
    override suspend fun getAllActiveItems(): List<Item> = itemDao.getAllActiveItems()
    override suspend fun getItemsByCategory(category: ItemCategory): List<Item> =
        itemDao.getItemsByCategory(category.name)
    override suspend fun getItemById(id: Int): Item? = itemDao.getItemById(id)
    override suspend fun getItemsByTier(tier: ItemTier): List<Item> =
        itemDao.getItemsByTier(tier.name)

    // Inventory
    override suspend fun getInventoryForUser(userId: Int): List<Inventory> =
        inventoryDao.getInventoryForUser(userId)
    override suspend fun getEquippedItems(userId: Int): List<Inventory> =
        inventoryDao.getEquippedItems(userId)
    override suspend fun getInventoryItem(userId: Int, itemId: Int): Inventory? =
        inventoryDao.getInventoryItem(userId, itemId)
    override suspend fun getInventoryWithItems(userId: Int): List<InventoryDao.InventoryWithItem> =
        inventoryDao.getInventoryWithItems(userId)
    override suspend fun insertInventoryItem(inventory: Inventory) =
        inventoryDao.insertInventoryItem(inventory)
    override suspend fun updateInventoryItem(inventory: Inventory) =
        inventoryDao.updateInventoryItem(inventory)
    override suspend fun unequipAllForUser(userId: Int) = inventoryDao.unequipAllForUser(userId)
    override suspend fun equipItem(inventoryId: Int) = inventoryDao.equipItem(inventoryId)
    override suspend fun unequipItem(inventoryId: Int) = inventoryDao.unequipItem(inventoryId)
    override suspend fun setEquipped(inventoryId: Int, equipped: Boolean) =
        inventoryDao.setEquipped(inventoryId, equipped)

    // Outfit
    override suspend fun getOutfit(userId: Int): Outfit? = outfitDao.getOutfit(userId)
    override fun getOutfitStream(userId: Int): Flow<Outfit> = outfitDao.getOutfitStream(userId)
    override suspend fun insertOutfit(outfit: Outfit) = outfitDao.insertOutfit(outfit)
    override suspend fun updateOutfit(outfit: Outfit) = outfitDao.updateOutfit(outfit)

    // Statistics
    override suspend fun getStatistics(): Statistics {
        val tasks = getAllTasks()
        val habits = getAllHabits().filter { it.isHabit }
        val completedTasks = tasks.count { it.isCompleted }
        val totalXpEarned = tasks.filter { it.isCompleted }.sumOf { it.xpReward } +
                habits.sumOf { it.xpReward * it.streak }
        val totalCoinsEarned = tasks.filter { it.isCompleted }.sumOf { it.coinReward } +
                habits.sumOf { it.coinReward * it.streak }

        val currentStreak = habits.maxOfOrNull { it.streak } ?: 0
        
        // Get all habit progress in one query (avoid N+1)
        val allProgress = habitProgressDao.getAllProgress()
        
        // Calculate longest streak from HabitProgress history
        val longestStreak = if (habits.isNotEmpty()) {
            habits.maxOf { habit ->
                calculateLongestStreakForHabit(allProgress.filter { it.habitId == habit.id })
            }
        } else 0
        
        val totalHabits = habits.size
        val totalTasks = tasks.size

        val now = System.currentTimeMillis()
        val weekAgo = now - 7 * MILLIS_PER_DAY
        val monthAgo = now - 30 * MILLIS_PER_DAY

        // epoch day conversion
        val weekAgoEpochDay = weekAgo / MILLIS_PER_DAY
        val monthAgoEpochDay = monthAgo / MILLIS_PER_DAY

        val weeklyCompletions = allProgress.count { it.date >= weekAgoEpochDay && it.completed }
        val monthlyCompletions = allProgress.count { it.date >= monthAgoEpochDay && it.completed }

        // Total possible completions: habits * days in period
        val totalPossibleWeekly = habits.size * 7
        val totalPossibleMonthly = habits.size * 30

        val weeklyCompletionRate = if (totalPossibleWeekly > 0) weeklyCompletions.toFloat() / totalPossibleWeekly else 0f
        val monthlyCompletionRate = if (totalPossibleMonthly > 0) monthlyCompletions.toFloat() / totalPossibleMonthly else 0f

        return Statistics(
            totalTasksCompleted = completedTasks,
            totalXpEarned = totalXpEarned,
            totalCoinsEarned = totalCoinsEarned,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            habitsCount = totalHabits,
            tasksCount = totalTasks,
            weeklyCompletionRate = weeklyCompletionRate,
            monthlyCompletionRate = monthlyCompletionRate
        )
    }
    
    private fun calculateLongestStreakForHabit(progress: List<HabitProgress>): Int {
        if (progress.isEmpty()) return 0
        
        // Sort by date descending
        val sorted = progress.sortedByDescending { it.date }
        var longest = 0
        var current = 0
        var lastDate: Long? = null
        
        for (p in sorted) {
            if (p.completed) {
                if (lastDate != null && p.date == lastDate - 1) {
                    current++
                } else {
                    current = 1
                }
                lastDate = p.date
                longest = maxOf(longest, current)
            } else {
                // Reset on non-completion
                current = 0
                lastDate = null
            }
        }
        return longest
    }
}
