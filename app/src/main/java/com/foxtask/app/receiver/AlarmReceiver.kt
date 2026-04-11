package com.foxtask.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.foxtask.app.di.ServiceLocator
import com.foxtask.app.util.AlarmManagerHelper
import com.foxtask.app.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AlarmReceiver получает точные будильники от AlarmManager и показывает уведомления.
 * 
 * Регистрируется в AndroidManifest.xml с exported=true для работы после перезагрузки.
 */
class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_REMINDER = "com.foxtask.app.ACTION_REMINDER"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_REMINDER) {
            val pendingResult = goAsync() // Продлить жизнь receiver
            val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
            val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Напоминание"
            
            if (taskId != -1) {
                Log.i("AlarmReceiver", "Reminder triggered for task $taskId: $title")
                
                // Show notification immediately (synchronous)
                NotificationHelper.showReminder(
                    context = context,
                    taskId = taskId,
                    title = title
                )
                
                // Reschedule for next day if task still has reminder enabled
                CoroutineScope(Dispatchers.IO + ServiceLocator.globalExceptionHandler).launch {
                    try {
                        val taskDao = ServiceLocator.getDatabase().taskDao()
                        val task = taskDao.getTaskById(taskId)
                        if (task != null && task.reminderEnabled && !task.isHabit && task.reminderTime != null) {
                            val (hour, minute) = AlarmManagerHelper.parseReminderTime(task.reminderTime)
                            AlarmManagerHelper.scheduleReminder(
                                context = context,
                                taskId = task.id,
                                title = task.title,
                                hour = hour,
                                minute = minute
                            )
                            Log.i("AlarmReceiver", "Rescheduled reminder for task ${task.id} to next day")
                        }
                    } catch (e: Exception) {
                        Log.e("AlarmReceiver", "Failed to reschedule reminder", e)
                    } finally {
                        pendingResult.finish() // Завершить receiver
                    }
                }
            } else {
                Log.e("AlarmReceiver", "Invalid task ID in reminder intent")
                pendingResult.finish()
            }
        }
    }
}
