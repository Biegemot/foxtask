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
    suspend operator fun invoke(taskId: Int): Reward = withContext(Dispatchers.IO) {
        val task = repository.getTaskById(taskId) ?: return@withContext Reward(0, 0)
        if (task.isCompleted) return@withContext Reward(0, 0)

        // Рассчитываем награду с использованием CalculateXpRewardUseCase
        val xp = calculateXpRewardUseCase(
            isHabit = task.isHabit,
            priority = task.priority,
            streak = task.streak
        )
        val coins = if (task.isHabit) {
            // For habits, calculate coin reward similar to XP
            var coinReward = if (task.isHabit) 5 else 5
            if (task.isHabit && task.streak >= 7) {
                coinReward = (coinReward * 1.5).toInt()
            }
            coinReward
        } else {
            task.coinReward
        }

        // Помечаем задачу выполненной
        repository.setTaskCompleted(taskId, true)

        // Добавляем награду пользователю и пересчитываем уровень
        val user = repository.getUser()
        user?.let {
            val newXp = it.currentXp + xp
            val newCoins = it.coins + coins
            val (newLevel, _) = calculateLevelUseCase(newXp)
            repository.updateUser(newLevel, newXp, newCoins)
        }

        Reward(xp, coins)
    }
}
