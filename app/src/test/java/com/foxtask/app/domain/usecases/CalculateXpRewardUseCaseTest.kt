package com.foxtask.app.domain.usecases

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CalculateXpRewardUseCase.
 * 
 * Tests XP reward calculation based on task priority.
 */
class CalculateXpRewardUseCaseTest {
    
    private lateinit var useCase: CalculateXpRewardUseCase
    
    @Before
    fun setup() {
        useCase = CalculateXpRewardUseCase()
    }
    
    @Test
    fun `low priority task gives base XP`() {
        val xp = useCase(isHabit = false, priority = 2)
        
        assertTrue(xp > 0)
    }
    
    @Test
    fun `medium priority task gives more XP`() {
        val xp = useCase(isHabit = false, priority = 3)
        
        assertTrue(xp > 0)
    }
    
    @Test
    fun `high priority task gives most XP`() {
        val xp = useCase(isHabit = false, priority = 5)
        
        assertTrue(xp > 0)
    }
    
    @Test
    fun `habit gives different XP than task`() {
        val taskXp = useCase(isHabit = false, priority = 3)
        val habitXp = useCase(isHabit = true, priority = 3)
        
        assertTrue(taskXp != habitXp)
    }
    
    @Test
    fun `higher priority gives more XP`() {
        val lowXp = useCase(isHabit = false, priority = 1)
        val mediumXp = useCase(isHabit = false, priority = 3)
        val highXp = useCase(isHabit = false, priority = 5)
        
        assertTrue(mediumXp > lowXp)
        assertTrue(highXp > mediumXp)
    }
    
    @Test
    fun `streak multiplier increases XP`() {
        val noStreakXp = useCase(isHabit = true, priority = 3, streak = 0)
        val streakXp = useCase(isHabit = true, priority = 3, streak = 7)
        
        assertTrue(streakXp > noStreakXp)
    }
    
    @Test
    fun `XP is always positive`() {
        val xp1 = useCase(isHabit = false, priority = 1)
        val xp2 = useCase(isHabit = true, priority = 2, streak = 0)
        
        assertTrue(xp1 > 0)
        assertTrue(xp2 > 0)
    }
}
