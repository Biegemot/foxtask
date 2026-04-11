package com.foxtask.app.domain.usecases

import com.foxtask.app.data.local.entities.Task
import com.foxtask.app.data.models.Priority
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
    fun `low priority task gives 10 XP`() {
        val task = createTask(priority = Priority.LOW)
        
        val xp = useCase(task)
        
        assertEquals(10, xp)
    }
    
    @Test
    fun `medium priority task gives 20 XP`() {
        val task = createTask(priority = Priority.MEDIUM)
        
        val xp = useCase(task)
        
        assertEquals(20, xp)
    }
    
    @Test
    fun `high priority task gives 30 XP`() {
        val task = createTask(priority = Priority.HIGH)
        
        val xp = useCase(task)
        
        assertEquals(30, xp)
    }
    
    @Test
    fun `XP reward increases with priority`() {
        val lowTask = createTask(priority = Priority.LOW)
        val mediumTask = createTask(priority = Priority.MEDIUM)
        val highTask = createTask(priority = Priority.HIGH)
        
        val lowXp = useCase(lowTask)
        val mediumXp = useCase(mediumTask)
        val highXp = useCase(highTask)
        
        assertTrue(mediumXp > lowXp)
        assertTrue(highXp > mediumXp)
    }
    
    @Test
    fun `all XP rewards are positive`() {
        Priority.values().forEach { priority ->
            val task = createTask(priority = priority)
            val xp = useCase(task)
            assertTrue("XP for $priority should be positive", xp > 0)
        }
    }
    
    // Helper function to create test tasks
    private fun createTask(
        id: Int = 1,
        title: String = "Test Task",
        priority: Priority = Priority.MEDIUM
    ): Task {
        return Task(
            id = id,
            title = title,
            description = null,
            priority = priority,
            dueDate = null,
            isCompleted = false,
            isHabit = false,
            reminderEnabled = false,
            reminderTime = null,
            createdAt = System.currentTimeMillis(),
            completedAt = null
        )
    }
}
