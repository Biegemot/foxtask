package com.foxtask.app.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * PermissionHelper для проверки и запроса разрешений.
 * 
 * Основное использование: проверка разрешения SCHEDULE_EXACT_ALARM на Android 12+
 */
object PermissionHelper {
    
    /**
     * Проверить, может ли приложение планировать точные будильники.
     * 
     * На Android 12+ (API 31+) требуется разрешение SCHEDULE_EXACT_ALARM.
     * На более старых версиях разрешение не требуется.
     * 
     * @param context Контекст приложения
     * @return true если разрешение предоставлено или не требуется
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Permission not required on older versions
        }
    }
    
    /**
     * Открыть системные настройки для предоставления разрешения SCHEDULE_EXACT_ALARM.
     * 
     * Пользователь будет перенаправлен в настройки приложения, где сможет
     * включить разрешение "Alarms & reminders".
     * 
     * @param context Контекст приложения
     */
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
    
    /**
     * Проверить, нужно ли показывать объяснение пользователю.
     * 
     * Рекомендуется показывать диалог с объяснением перед первым запросом разрешения.
     * 
     * @param context Контекст приложения
     * @return true если нужно показать объяснение
     */
    fun shouldShowExactAlarmRationale(context: Context): Boolean {
        // На Android 12+ всегда показываем объяснение, если разрешение не предоставлено
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            !canScheduleExactAlarms(context)
        } else {
            false
        }
    }
}
