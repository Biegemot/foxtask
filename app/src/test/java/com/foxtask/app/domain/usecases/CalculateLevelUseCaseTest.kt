package com.foxtask.app.domain.usecases

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CalculateLevelUseCase.
 * 
 * Tests the XP to level calculation logic.
 */
class CalculateLevelUseCaseTest {
    
    private lateinit var useCase: CalculateLevelUseCase
    
    @Before
    fun setup() {
        useCase = CalculateLevelUseCase()
    }
    
    @Test
    fun `calculate level for 0 XP returns level 1`() {
        val result = useCase.calculateLevel(0)
        
        assertEquals(1, result.level)
        assertEquals(0, result.currentXp)
        assertTrue(result.xpForNextLevel > 0)
    }
    
    @Test
    fun `calculate level for 100 XP returns level 2`() {
        val result = useCase.calculateLevel(100)
        
        assertEquals(2, result.level)
        assertEquals(0, result.currentXp)
    }
    
    @Test
    fun `calculate level for 50 XP returns level 1 with progress`() {
        val result = useCase.calculateLevel(50)
        
        assertEquals(1, result.level)
        assertEquals(50, result.currentXp)
        assertTrue(result.xpForNextLevel > 0)
    }
    
    @Test
    fun `calculate level for 250 XP returns correct level`() {
        // 100 XP for level 2, 150 XP for level 3 = 250 total
        val result = useCase.calculateLevel(250)
        
        assertEquals(3, result.level)
        assertEquals(0, result.currentXp)
    }
    
    @Test
    fun `calculate level for 275 XP returns level 3 with progress`() {
        // 250 XP to reach level 3, 25 XP progress towards level 4
        val result = useCase.calculateLevel(275)
        
        assertEquals(3, result.level)
        assertEquals(25, result.currentXp)
        assertTrue(result.xpForNextLevel > 25)
    }
    
    @Test
    fun `XP for next level increases with each level`() {
        val level1 = useCase.calculateLevel(0)
        val level2 = useCase.calculateLevel(100)
        val level3 = useCase.calculateLevel(250)
        
        assertTrue(level2.xpForNextLevel > level1.xpForNextLevel)
        assertTrue(level3.xpForNextLevel > level2.xpForNextLevel)
    }
    
    @Test
    fun `negative XP is treated as 0`() {
        val result = useCase.calculateLevel(-100)
        
        assertEquals(1, result.level)
        assertEquals(0, result.currentXp)
    }
    
    @Test
    fun `very large XP returns high level`() {
        val result = useCase.calculateLevel(10000)
        
        assertTrue(result.level > 10)
        assertTrue(result.currentXp >= 0)
    }
}
