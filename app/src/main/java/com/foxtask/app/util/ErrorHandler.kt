package com.foxtask.app.util

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.IOException
import android.database.sqlite.SQLiteException

/**
 * ErrorHandler для централизованной обработки ошибок и отображения их пользователю.
 * 
 * Использует SharedFlow для передачи сообщений об ошибках в UI.
 */
object ErrorHandler {
    private val _errorFlow = MutableSharedFlow<String>(replay = 0)
    val errorFlow: SharedFlow<String> = _errorFlow.asSharedFlow()
    
    /**
     * Обработать ошибку и отправить сообщение пользователю.
     * 
     * @param error Исключение для обработки
     * @param userMessage Пользовательское сообщение (опционально)
     */
    suspend fun handleError(error: Throwable, userMessage: String? = null) {
        Log.e("ErrorHandler", "Error occurred", error)
        
        val message = userMessage ?: when (error) {
            is IOException -> "Ошибка сети. Проверьте подключение"
            is SQLiteException -> "Ошибка базы данных"
            is IllegalStateException -> "Некорректное состояние приложения"
            is IllegalArgumentException -> "Некорректные данные"
            else -> "Произошла ошибка. Попробуйте снова"
        }
        
        _errorFlow.emit(message)
    }
    
    /**
     * Отправить пользовательское сообщение об ошибке.
     * 
     * @param message Сообщение для отображения
     */
    suspend fun showError(message: String) {
        Log.w("ErrorHandler", "User error: $message")
        _errorFlow.emit(message)
    }
}
