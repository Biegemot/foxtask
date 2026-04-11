package com.foxtask.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.foxtask.app.data.local.entities.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isHabit = 0 ORDER BY dueDate ASC, priority DESC")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE isHabit = 1 ORDER BY title ASC")
    suspend fun getAllHabits(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Int, completed: Boolean)

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND (dueDate <= :beforeDate OR dueDate IS NULL)")
    suspend fun getPendingTasks(beforeDate: Long): List<Task>
    
    @Query("SELECT id FROM tasks WHERE reminderEnabled = 1")
    suspend fun getTaskIdsWithReminders(): List<Int>
}
