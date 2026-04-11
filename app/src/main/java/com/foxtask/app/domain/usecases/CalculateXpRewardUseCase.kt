package com.foxtask.app.domain.usecases

import com.foxtask.app.domain.interfaces.GameRules

class CalculateXpRewardUseCase {
    operator fun invoke(
        isHabit: Boolean,
        priority: Int = 3,
        streak: Int = 0
    ): Int {
        val baseXp = if (isHabit) GameRules.BASE_HABIT_XP else GameRules.BASE_TASK_XP
        var reward = baseXp + (priority - 3) * GameRules.PRIORITY_BONUS_PER_POINT
        if (isHabit && streak >= GameRules.STREAK_THRESHOLD_FOR_BONUS) {
            reward = (reward * GameRules.STREAK_BONUS_MULTIPLIER).toInt()
        }
        return reward
    }
}
