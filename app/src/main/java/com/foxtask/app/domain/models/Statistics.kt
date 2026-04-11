package com.foxtask.app.domain.models

data class Statistics(
    val totalTasksCompleted: Int,
    val totalXpEarned: Int,
    val totalCoinsEarned: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val habitsCount: Int,
    val tasksCount: Int,
    val weeklyCompletionRate: Float, // 0..1
    val monthlyCompletionRate: Float
)
