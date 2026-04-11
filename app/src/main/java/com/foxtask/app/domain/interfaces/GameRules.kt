package com.foxtask.app.domain.interfaces

object GameRules {
    // XP formula: 100 * level^2
    const val BASE_XP_FOR_LEVEL = 100

    // Rewards
    const val BASE_TASK_XP = 15
    const val BASE_HABIT_XP = 10
    const val BASE_TASK_COINS = 5
    const val BASE_HABIT_COINS = 5
    const val PRIORITY_BONUS_PER_POINT = 5
    const val STREAK_BONUS_MULTIPLIER = 1.5f
    const val STREAK_THRESHOLD_FOR_BONUS = 7

    // Item tiers
    const val COMMON_MIN_PRICE = 50
    const val COMMON_MAX_PRICE = 150
    const val RARE_MIN_PRICE = 200
    const val RARE_MAX_PRICE = 400
    const val EPIC_MIN_PRICE = 500
    const val EPIC_MAX_PRICE = 1000

    // Items per category
    const val COMMON_ITEMS_PER_CATEGORY = 4
    const val RARE_ITEMS_PER_CATEGORY = 2
    const val EPIC_ITEMS_PER_CATEGORY = 1
}
