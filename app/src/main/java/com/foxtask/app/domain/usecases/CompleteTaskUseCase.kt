package com.foxtask.app.domain.usecases

import com.foxtask.app.data.repository.FoxTaskRepository
import com.foxtask.app.domain.models.Reward
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CompleteTaskUseCase(
    private val repository: FoxTaskRepository,
    private val calculateLevelUseCase: CalculateLevelUseCase,
    private val calculateXpRewardUseCase: CalculateXpRewardUseCase = CalculateXpRewardUseCase()
) {
    companion object {
        private const val MAX_XP = 999_999
        private const val MAX_COINS = 999_999
        private const val MAX_LEVEL = 100
    }
    
    suspend operator fun invoke(taskId: Int): Reward = withContext(Dispatchers.IO) {
        val task = repository.getTaskById(taskId) ?: return@withContext Reward(0, 0)
        if (task.isCompleted) return@withContext Reward(0, 0)

        // Рассчитываем награду с использованием CalculateXpRewardUseCase
        val xp = calculateXpRewardUseCase(
            isHabit = task.isHabit,
            priority = task.priority,
            streak = task.streak
        )
        val coins = task.coinReward

        // Помечаем задачу выполненной
        repository.setTaskCompleted(taskId, true)

        // Добавляем награду пользователю и пересчитываем уровень
        val user = repository.getUser()
        user?.let {
            // Validate and cap values to prevent overflow
            val newXp = (it.currentXp + xp).coerceIn(0, MAX_XP)
            val newCoins = (it.coins + coins).coerceIn(0, MAX_COINS)
            val (newLevel, _) = calculateLevelUseCase(newXp)
            val cappedLevel = newLevel.coerceIn(1, MAX_LEVEL)
            
            repository.updateUser(cappedLevel, newXp, newCoins)
        }

        Reward(xp, coins)
    }
}
