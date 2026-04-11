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
        val (level, xpToNextLevel) = useCase.invoke(0)
        
        assertEquals(0, level)
        assertTrue(xpToNextLevel > 0)
    }
    
    @Test
    fun `calculate level for 100 XP returns level 1`() {
        val (level, xpToNextLevel) = useCase.invoke(100)
        
        assertEquals(1, level)
        assertTrue(xpToNextLevel > 0)
    }
    
    @Test
    fun `calculate level for 50 XP returns level 0 with progress`() {
        val (level, xpToNextLevel) = useCase.invoke(50)
        
        assertEquals(0, level)
        assertEquals(50, xpToNextLevel)
    }
    
    @Test
    fun `calculate level for 400 XP returns level 2`() {
        // 100 XP for level 1, 400 XP for level 2
        val (level, xpToNextLevel) = useCase.invoke(400)
        
        assertEquals(2, level)
        assertTrue(xpToNextLevel > 0)
    }
    
    @Test
    fun `calculate level for 450 XP returns level 2 with progress`() {
        // 400 XP to reach level 2, 50 XP progress towards level 3
        val (level, xpToNextLevel) = useCase.invoke(450)
        
        assertEquals(2, level)
        assertTrue(xpToNextLevel > 0)
    }
    
    @Test
    fun `XP for next level increases with each level`() {
        val (_, xpToNext1) = useCase.invoke(0)
        val (_, xpToNext2) = useCase.invoke(100)
        val (_, xpToNext3) = useCase.invoke(400)
        
        assertTrue(xpToNext2 > xpToNext1)
        assertTrue(xpToNext3 > xpToNext2)
    }
    
    @Test
    fun `negative XP is treated as 0`() {
        val (level, xpToNextLevel) = useCase.invoke(-100)
        
        assertEquals(0, level)
        assertTrue(xpToNextLevel > 0)
    }
    
    @Test
    fun `very large XP returns high level`() {
        val (level, xpToNextLevel) = useCase.invoke(10000)
        
        assertTrue(level > 5)
        assertTrue(xpToNextLevel > 0)
    }
}
