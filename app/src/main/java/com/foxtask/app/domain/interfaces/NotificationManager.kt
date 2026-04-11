package com.foxtask.app.domain.interfaces

import android.app.Notification

interface NotificationManager {
    fun showReminder(taskId: Int, title: String)
    fun cancelReminder(taskId: Int)
    fun createNotification(
        taskId: Int,
        title: String,
        description: String?,
        soundEnabled: Boolean,
        vibrateEnabled: Boolean
    ): Notification
}
