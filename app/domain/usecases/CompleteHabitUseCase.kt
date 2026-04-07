package com.foxtask.app.domain.usecases

import com.foxtask.app.data.repository.FoxTaskRepository
import com.foxtask.app.domain.models.Reward
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class CompleteHabitUseCase(
    private val repository: FoxTaskRepository,
    private val calculateLevelUseCase: CalculateLevelUseCase
) {
    suspend operator fun invoke(habitId: Int, completedDate: Long? = null): Reward =
        withContext(Dispatchers.IO) {
            val habit = repository.getTaskById(habitId) ?: return@withContext Reward(0, 0)
            val today = completedDate ?: System.currentTimeMillis()
            // Use consistent epoch day calculation (milliseconds per day = 86400000)
            val epochDay = today / 86400000

            // Проверяем прогресс на сегодня
            val existing = repository.getProgressForDate(habitId, epochDay)
            if (existing != null) {
                // Уже выполнено сегодня
                return@withContext Reward(0, 0)
            }

            // Определяем текущий стрик
            val lastStreak = habit.streak
            var newStreak = 1

            // Проверяем вчерашний день
            val yesterday = epochDay - 1
            val yesterdayProgress = repository.getProgressForDate(habitId, yesterday)
            if (yesterdayProgress != null) {
                newStreak = lastStreak + 1
            }

            // Рассчитываем награду с учетом стрика
            var xp = habit.xpReward
            var coins = habit.coinReward

            // Бонус за стрик >=7 дней
            val multiplier = if (newStreak >= 7) 1.5 else 1.0
            xp = (xp * multiplier).toInt()
            coins = (coins * multiplier).toInt()

            // Сохраняем прогресс
            repository.insertProgress(
                com.foxtask.app.data.local.entities.HabitProgress(
                    habitId = habitId,
                    date = epochDay,
                    completed = true
                )
            )

            // Обновляем стрик привычки
            val updatedHabit = habit.copy(
                streak = newStreak,
                lastCompletedDate = today
            )
            repository.updateTask(updatedHabit)

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
