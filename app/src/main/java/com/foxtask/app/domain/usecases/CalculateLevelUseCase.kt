package com.foxtask.app.domain.usecases

import kotlin.math.sqrt

class CalculateLevelUseCase {
    // XP для уровня N: 100 * N^2
    // Уровень = floor(sqrt(currentXp / 100))
    // XP до следующего = 100 * (level+1)^2 - currentXp

    operator fun invoke(currentXp: Int): Pair<Int, Int> {
        // Use double division to avoid integer division issues
        val level = sqrt((currentXp.toDouble() / 100.0)).toInt()
        val xpToNextLevel = 100 * (level + 1) * (level + 1) - currentXp
        return Pair(level, xpToNextLevel)
    }
}
