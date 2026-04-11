package com.foxtask.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.foxtask.app.di.ServiceLocator
import com.foxtask.app.receiver.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AlarmManagerHelper для точных напоминаний.
 * 
 * Использует AlarmManager.setExactAndAllowWhileIdle() для точного срабатывания в указанное время.
 * В отличие от WorkManager, AlarmManager может запустить задачу в точное время (с погрешностью ~1 минуты).
 * 
 * Требует разрешения SCHEDULE_EXACT_ALARM в AndroidManifest.xml (уже добавлено).
 */
object AlarmManagerHelper {
    private const val TAG = "AlarmManagerHelper"
    private const val REQUEST_CODE_BASE = 1000
    private const val MILLIS_PER_DAY = 86_400_000L
    private const val DEFAULT_REMINDER_HOUR = 9
    private const val DEFAULT_REMINDER_MINUTE = 0

    /**
     * Парсит время в формате "HH:mm" в пару (hour, minute).
     */
    fun parseReminderTime(timeString: String): Pair<Int, Int> {
        return try {
            val parts = timeString.split(":")
            val hour = parts[0].toInt()
            val minute = if (parts.size > 1) parts[1].toInt() else 0
            Pair(hour, minute)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid time format: $timeString, using $DEFAULT_REMINDER_HOUR:$DEFAULT_REMINDER_MINUTE")
            Pair(DEFAULT_REMINDER_HOUR, DEFAULT_REMINDER_MINUTE)
        }
    }

    /**
     * scheduled exact reminder at specified time
     * @param context Application context
     * @param taskId Task ID to identify the reminder
     * @param title Task title for notification
     * @param hour Hour of day (0-23)
     * @param minute Minute (0-59)
     */
    fun scheduleReminder(
        context: Context,
        taskId: Int,
        title: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Check permission for Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!PermissionHelper.canScheduleExactAlarms(context)) {
                Log.e(TAG, "Missing SCHEDULE_EXACT_ALARM permission on Android 12+")
                // Notify user through ErrorHandler
                CoroutineScope(Dispatchers.Main).launch {
                    ErrorHandler.showError(context.getString(com.foxtask.app.R.string.permission_exact_alarm_required))
                }
                return
            }
        }
        
        // Calculate trigger time
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        // If time already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        
        val triggerAtMillis = calendar.timeInMillis
        
        // Create intent for AlarmReceiver
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, title)
        }
        
        // Create pending intent with proper security flags
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId, // unique request code per task
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        
        try {
            // Cancel existing alarm if any
            alarmManager.cancel(pendingIntent)
            
            // Schedule exact alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6.0+ use setExactAndAllowWhileIdle for exact timing
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // For Android 4.4+ use setExact
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // For older versions use set (less accurate)
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            
            Log.i(TAG, "Reminder scheduled for task $taskId at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule alarm: missing SCHEDULE_EXACT_ALARM permission", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }
    
    /**
     * Cancel reminder for specific task
     */
    fun cancelReminder(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.i(TAG, "Reminder cancelled for task $taskId")
    }
    
     /**
      * Cancel all reminders (e.g., on logout)
      */
     suspend fun cancelAllReminders(context: Context) {
         val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
         
         // Get actual task IDs with reminders from database
         val taskIds = try {
             ServiceLocator.getDatabase().taskDao().getTaskIdsWithReminders()
         } catch (e: Exception) {
             Log.e(TAG, "Failed to get task IDs from database", e)
             emptyList()
         }
         
         taskIds.forEach { taskId ->
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_REMINDER
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
        }
        
        Log.i(TAG, "Cancelled ${taskIds.size} reminders")
    }
}
