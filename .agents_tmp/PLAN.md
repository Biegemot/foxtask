# 1. OBJECTIVE

Устранить выявленные и потенциальные проблемы в Android-приложении FoxTask, начиная с критических ошибок высокого приоритета и заканчивая оптимизациями низкого приоритета. Цель — обеспечить стабильность, производительность и качество кода перед релизом приложения.

# 2. CONTEXT SUMMARY

**Проект:** FoxTask — Android-приложение для управления задачами с геймификацией  
**Технологии:** Kotlin, Jetpack Compose, Room Database, Material 3, AlarmManager  
**Архитектура:** Clean Architecture (data/domain/presentation)  
**Размер кодовой базы:** 61 Kotlin-файл, 47 XML-ресурсов  

**Выявленные проблемы (по приоритетам):**

### Высокий приоритет (критические):
1. **Утечка памяти в MainActivity** — CoroutineScope.launch без привязки к lifecycle (строка 43)
2. **Потенциальная утечка в BroadcastReceivers** — использование CoroutineScope без goAsync() может привести к преждевременному завершению
3. **Отсутствие обработки ошибок UI** — GlobalExceptionHandler только логирует, не показывает пользователю
4. **Безопасность: android:exported="true"** — AlarmReceiver и BootReceiver открыты для внешних приложений

### Средний приоритет (функциональные):
5. **Неоптимальная реализация cancelAllReminders()** — перебор 1000 ID вместо хранения активных
6. **Отсутствие ProGuard rules** — может привести к проблемам в release сборке (Room, Compose)
7. **Отсутствие обработки разрешений** — нет UI для запроса SCHEDULE_EXACT_ALARM на Android 12+
8. **Отсутствие unit-тестов** — нет покрытия для Use Cases и бизнес-логики
9. **Отсутствие индексов для часто используемых запросов** — reminderEnabled в TaskDao

### Низкий приоритет (оптимизации):
10. **Hardcoded strings** — некоторые строки не вынесены в strings.xml
11. **Отсутствие обработки edge cases** — переполнение XP/монет, отрицательные значения
12. **Compose recomposition** — потенциальные проблемы производительности
13. **Отсутствие аналитики ошибок** — нет интеграции с Crashlytics/Sentry
14. **Дублирование кода** — похожая логика в AlarmReceiver и BootReceiver

**Зависимости между проблемами:**
- Проблемы 1-3 связаны с обработкой ошибок и lifecycle
- Проблемы 5, 9 связаны с производительностью БД
- Проблемы 6-7 критичны для production релиза

# 3. APPROACH OVERVIEW

Исправление проблем будет проводиться в 3 фазы по приоритетам:

## Фаза 1: Критические проблемы (Высокий приоритет)
Устранение проблем, которые могут привести к крашам, утечкам памяти или уязвимостям безопасности. Эти исправления необходимы для стабильной работы приложения.

**Проблемы:** 1-4  
**Время:** ~4-6 часов  
**Риск:** Средний (изменения в core компонентах)

## Фаза 2: Функциональные улучшения (Средний приоритет)
Исправление функциональных недостатков и добавление необходимых компонентов для production релиза. Эти изменения улучшат UX и подготовят приложение к релизу.

**Проблемы:** 5-9  
**Время:** ~6-8 часов  
**Риск:** Низкий (изолированные изменения)

## Фаза 3: Оптимизации (Низкий приоритет)
Улучшение качества кода, производительности и maintainability. Эти изменения можно отложить на следующие версии.

**Проблемы:** 10-14  
**Время:** ~4-6 часов  
**Риск:** Минимальный (рефакторинг)

**Стратегия тестирования:**
- После каждой фазы — сборка и запуск приложения
- Проверка критических сценариев (создание задачи, напоминания, покупка предметов)
- Тестирование на разных версиях Android (API 24, 31, 34)

# 4. IMPLEMENTATION STEPS

---
## ФАЗА 1: КРИТИЧЕСКИЕ ПРОБЛЕМЫ (ВЫСОКИЙ ПРИОРИТЕТ)
---

### Проблема 1: Утечка памяти в MainActivity (строка 43)

**Цель:** Исправить утечку памяти из-за CoroutineScope без привязки к lifecycle  
**Файл:** `app/src/main/java/com/foxtask/app/MainActivity.kt`

**Метод:**
1. Удалить строки 36-45 (неправильный GlobalExceptionHandler setup)
2. GlobalExceptionHandler уже инициализирован в ServiceLocator.init()
3. Не нужно создавать отдельный CoroutineScope в Activity
4. Если нужна инициализация, использовать lifecycleScope

**Код для удаления:**
```kotlin
// Строки 36-45
val globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
    Log.e("GlobalExceptionHandler", "Unhandled coroutine exception", exception)
    // TODO: Show user-friendly UI notification
}

CoroutineScope(SupervisorJob() + Dispatchers.Main + globalExceptionHandler).launch {
    // This scope will handle uncaught exceptions
}
```

**Результат:** Устранение утечки памяти и дублирования кода

---

### Проблема 2: Потенциальная утечка в BroadcastReceivers

**Цель:** Обеспечить корректное выполнение асинхронных операций в BroadcastReceiver  
**Файлы:** 
- `app/src/main/java/com/foxtask/app/receiver/AlarmReceiver.kt`
- `app/src/main/java/com/foxtask/app/receiver/BootReceiver.kt`

**Метод:**
1. Использовать `goAsync()` для продления жизни BroadcastReceiver
2. Вызвать `pendingResult.finish()` после завершения coroutine
3. Добавить timeout для предотвращения зависания

**Изменения в AlarmReceiver.kt (строка 27):**
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == ACTION_REMINDER) {
        val pendingResult = goAsync() // Продлить жизнь receiver
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Напоминание"
        
        if (taskId != -1) {
            // Show notification immediately (synchronous)
            NotificationHelper.showReminder(...)
            
            // Reschedule in background
            CoroutineScope(Dispatchers.IO + ServiceLocator.globalExceptionHandler).launch {
                try {
                    // ... existing code ...
                } finally {
                    pendingResult.finish() // Завершить receiver
                }
            }
        } else {
            pendingResult.finish()
        }
    }
}
```

**Аналогичные изменения в BootReceiver.kt**

**Результат:** Гарантированное выполнение асинхронных операций

---

### Проблема 3: Отсутствие обработки ошибок UI

**Цель:** Показывать пользователю понятные сообщения об ошибках  
**Файлы:**
- `app/src/main/java/com/foxtask/app/di/ServiceLocator.kt`
- Создать новый: `app/src/main/java/com/foxtask/app/util/ErrorHandler.kt`

**Метод:**
1. Создать ErrorHandler с SharedFlow для передачи ошибок в UI
2. Подписаться на ошибки в MainActivity
3. Показывать Snackbar с сообщением об ошибке

**Создать ErrorHandler.kt:**
```kotlin
object ErrorHandler {
    private val _errorFlow = MutableSharedFlow<String>(replay = 0)
    val errorFlow: SharedFlow<String> = _errorFlow.asSharedFlow()
    
    suspend fun handleError(error: Throwable, userMessage: String? = null) {
        Log.e("ErrorHandler", "Error occurred", error)
        val message = userMessage ?: when (error) {
            is IOException -> "Ошибка сети. Проверьте подключение"
            is SQLiteException -> "Ошибка базы данных"
            else -> "Произошла ошибка. Попробуйте снова"
        }
        _errorFlow.emit(message)
    }
}
```

**Обновить ServiceLocator.kt (строка 26):**
```kotlin
globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
    Log.e("GlobalExceptionHandler", "Unhandled coroutine exception", exception)
    CoroutineScope(Dispatchers.Main).launch {
        ErrorHandler.handleError(exception)
    }
}
```

**Обновить MainActivity.kt (добавить в MainScreenContent):**
```kotlin
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(Unit) {
    ErrorHandler.errorFlow.collect { message ->
        snackbarHostState.showSnackbar(message)
    }
}

Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    // ... rest of scaffold
)
```

**Результат:** Пользователь видит понятные сообщения об ошибках

---

### Проблема 4: Безопасность android:exported="true"

**Цель:** Защитить BroadcastReceivers от внешних приложений  
**Файл:** `app/src/main/AndroidManifest.xml`

**Метод:**
1. Изменить `android:exported="false"` для AlarmReceiver
2. Оставить `android:exported="true"` для BootReceiver (требуется системой)
3. Добавить проверку отправителя в AlarmReceiver

**Изменения в AndroidManifest.xml (строка 34):**
```xml
<!-- AlarmReceiver for exact-time reminders -->
<receiver
    android:name=".receiver.AlarmReceiver"
    android:exported="false"
    android:enabled="true" />
```

**Добавить проверку в AlarmReceiver.kt (строка 27):**
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    // Verify intent is from our app
    if (intent.action == ACTION_REMINDER) {
        val callingPackage = intent.getStringExtra("calling_package")
        if (callingPackage != null && callingPackage != context.packageName) {
            Log.w("AlarmReceiver", "Ignoring intent from external app: $callingPackage")
            return
        }
        // ... rest of code
    }
}
```

**Результат:** Защита от несанкционированного доступа

---
## ФАЗА 2: ФУНКЦИОНАЛЬНЫЕ УЛУЧШЕНИЯ (СРЕДНИЙ ПРИОРИТЕТ)
---

### Проблема 5: Неоптимальная реализация cancelAllReminders()

**Цель:** Оптимизировать отмену напоминаний через хранение активных ID  
**Файлы:**
- `app/src/main/java/com/foxtask/app/util/AlarmManagerHelper.kt`
- `app/src/main/java/com/foxtask/app/data/local/dao/TaskDao.kt`

**Метод:**
1. Добавить запрос в TaskDao для получения всех задач с активными напоминаниями
2. Использовать реальные ID вместо перебора 1..1000
3. Удалить константу MAX_TASK_ID

**Обновить TaskDao.kt:**
```kotlin
@Query("SELECT id FROM tasks WHERE reminderEnabled = 1")
suspend fun getTaskIdsWithReminders(): List<Int>
```

**Обновить AlarmManagerHelper.kt (строка 162):**
```kotlin
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
```

**Результат:** Оптимизация с O(1000) до O(n), где n — реальное количество напоминаний

---

### Проблема 6: Отсутствие ProGuard rules

**Цель:** Добавить правила ProGuard для корректной работы release сборки  
**Файл:** Создать `app/proguard-rules.pro`

**Метод:**
1. Создать файл proguard-rules.pro
2. Добавить правила для Room, Kotlin Coroutines, Compose
3. Включить minification в build.gradle.kts

**Создать app/proguard-rules.pro:**
```proguard
# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Keep data classes
-keep class com.foxtask.app.data.local.entities.** { *; }
-keep class com.foxtask.app.data.models.** { *; }
-keep class com.foxtask.app.domain.models.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
```

**Обновить app/build.gradle.kts (строка 26):**
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

**Результат:** Корректная работа release сборки с оптимизацией

---

### Проблема 7: Отсутствие обработки разрешений SCHEDULE_EXACT_ALARM

**Цель:** Добавить UI для запроса разрешения на Android 12+  
**Файлы:**
- `app/src/main/java/com/foxtask/app/util/PermissionHelper.kt` (создать)
- `app/src/main/java/com/foxtask/app/presentation/ui/screens/TaskEditScreen.kt`

**Метод:**
1. Создать PermissionHelper для проверки и запроса разрешения
2. Показывать диалог с объяснением перед запросом
3. Открывать системные настройки для предоставления разрешения

**Создать PermissionHelper.kt:**
```kotlin
object PermissionHelper {
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Permission not required on older versions
        }
    }
    
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
}
```

**Добавить в TaskEditScreen.kt (перед scheduleReminder):**
```kotlin
// Check permission before scheduling
if (!PermissionHelper.canScheduleExactAlarms(context)) {
    showPermissionDialog = true
} else {
    AlarmManagerHelper.scheduleReminder(...)
}

// Permission dialog
if (showPermissionDialog) {
    AlertDialog(
        onDismissRequest = { showPermissionDialog = false },
        title = { Text("Требуется разрешение") },
        text = { Text("Для точных напоминаний необходимо разрешение. Разрешить в настройках?") },
        confirmButton = {
            TextButton(onClick = {
                PermissionHelper.requestExactAlarmPermission(context)
                showPermissionDialog = false
            }) {
                Text("Настройки")
            }
        },
        dismissButton = {
            TextButton(onClick = { showPermissionDialog = false }) {
                Text("Отмена")
            }
        }
    )
}
```

**Результат:** Пользователь может предоставить разрешение через UI

---

### Проблема 8: Отсутствие unit-тестов

**Цель:** Добавить базовое покрытие тестами для Use Cases  
**Файлы:** Создать в `app/src/test/java/com/foxtask/app/domain/usecases/`

**Метод:**
1. Создать тесты для CalculateLevelUseCase
2. Создать тесты для CalculateXpRewardUseCase
3. Создать тесты для CompleteTaskUseCase (с моками)

**Создать CalculateLevelUseCaseTest.kt:**
```kotlin
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
    }
    
    @Test
    fun `calculate level for 100 XP returns level 2`() {
        val result = useCase.calculateLevel(100)
        assertEquals(2, result.level)
    }
    
    @Test
    fun `calculate XP for next level is correct`() {
        val result = useCase.calculateLevel(50)
        assertEquals(1, result.level)
        assertEquals(50, result.currentXp)
        assertTrue(result.xpForNextLevel > 0)
    }
}
```

**Добавить зависимости в app/build.gradle.kts:**
```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.3.1")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
```

**Результат:** Базовое покрытие критической бизнес-логики

---

### Проблема 9: Отсутствие индексов для reminderEnabled

**Цель:** Добавить индекс для оптимизации запросов с reminderEnabled  
**Файлы:**
- `app/src/main/java/com/foxtask/app/data/local/FoxTaskDatabase.kt`
- `app/src/main/java/com/foxtask/app/data/local/entities/Task.kt`

**Метод:**
1. Добавить индекс в миграцию MIGRATION_1_2
2. Добавить аннотацию @Index в Task entity
3. Увеличить версию БД до 3 и создать MIGRATION_2_3

**Обновить FoxTaskDatabase.kt (версия и миграция):**
```kotlin
@Database(
    entities = [...],
    version = 3, // Увеличить версию
    exportSchema = false,
    typeConverters = [Converters::class]
)
abstract class FoxTaskDatabase : RoomDatabase() {
    // ...
    
    fun getInstance(context: Context): FoxTaskDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(...)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
    
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_reminderEnabled ON tasks(reminderEnabled)")
        }
    }
}
```

**Обновить Task.kt (добавить индекс):**
```kotlin
@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["reminderEnabled"]),
        Index(value = ["isHabit", "isCompleted"])
    ]
)
data class Task(...)
```

**Результат:** Ускорение запросов с фильтрацией по reminderEnabled

---
## ФАЗА 3: ОПТИМИЗАЦИИ (НИЗКИЙ ПРИОРИТЕТ)
---

### Проблема 10: Hardcoded strings

**Цель:** Вынести hardcoded строки в strings.xml  
**Файлы:** Все `.kt` файлы с UI, `res/values/strings.xml`

**Метод:**
1. Найти все hardcoded строки в Compose UI
2. Добавить их в strings.xml
3. Использовать stringResource() в Compose

**Примеры для добавления в strings.xml:**
```xml
<string name="main_screen_level">Уровень %1$d</string>
<string name="action_tasks">Задачи</string>
<string name="action_shop">Магазин</string>
<string name="action_wardrobe">Гардероб</string>
<string name="action_stats">Статистика</string>
<string name="error_network">Ошибка сети. Проверьте подключение</string>
<string name="error_database">Ошибка базы данных</string>
<string name="error_generic">Произошла ошибка. Попробуйте снова</string>
<string name="permission_exact_alarm_title">Требуется разрешение</string>
<string name="permission_exact_alarm_message">Для точных напоминаний необходимо разрешение. Разрешить в настройках?</string>
```

**Результат:** Поддержка локализации и централизованное управление текстами

---

### Проблема 11: Отсутствие обработки edge cases

**Цель:** Добавить валидацию для XP, монет и других числовых значений  
**Файлы:** `domain/usecases/*.kt`

**Метод:**
1. Добавить проверки на отрицательные значения
2. Добавить проверки на переполнение (Int.MAX_VALUE)
3. Добавить константы для максимальных значений

**Обновить CompleteTaskUseCase.kt:**
```kotlin
companion object {
    private const val MAX_XP = 999_999
    private const val MAX_COINS = 999_999
    private const val MAX_LEVEL = 100
}

suspend operator fun invoke(taskId: Int): Result<Reward> {
    return try {
        // ... existing code ...
        
        // Validate and cap values
        val cappedXp = (user.currentXp + xpReward).coerceIn(0, MAX_XP)
        val cappedCoins = (user.coins + coinReward).coerceIn(0, MAX_COINS)
        val cappedLevel = newLevel.coerceIn(1, MAX_LEVEL)
        
        repository.updateUser(user.copy(
            currentXp = cappedXp,
            coins = cappedCoins,
            level = cappedLevel
        ))
        
        Result.success(Reward(xpReward, coinReward, leveledUp))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Результат:** Защита от некорректных значений

---

### Проблема 12: Compose recomposition оптимизация

**Цель:** Оптимизировать recomposition в Compose UI  
**Файлы:** `presentation/ui/screens/*.kt`, `presentation/ui/components/*.kt`

**Метод:**
1. Использовать `derivedStateOf` для вычисляемых значений
2. Использовать `key()` в LazyColumn
3. Стабилизировать lambda параметры с `remember`

**Пример оптимизации в TasksScreen.kt:**
```kotlin
// Before
val filteredTasks = tasks.filter { !it.isCompleted }

// After
val filteredTasks by remember(tasks) {
    derivedStateOf { tasks.filter { !it.isCompleted } }
}

// LazyColumn with keys
LazyColumn {
    items(
        items = filteredTasks,
        key = { task -> task.id } // Stable key
    ) { task ->
        TaskCard(
            task = task,
            onClick = remember { { onTaskClick(task.id) } } // Stable lambda
        )
    }
}
```

**Результат:** Уменьшение ненужных recomposition

---

### Проблема 13: Отсутствие аналитики ошибок

**Цель:** Добавить базовую аналитику ошибок (опционально)  
**Файлы:** `build.gradle.kts`, `ErrorHandler.kt`

**Метод:**
1. Добавить зависимость на Firebase Crashlytics (опционально)
2. Логировать критические ошибки
3. Добавить custom keys для контекста

**Добавить в app/build.gradle.kts (опционально):**
```kotlin
dependencies {
    // Firebase Crashlytics (optional)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
}
```

**Обновить ErrorHandler.kt:**
```kotlin
fun logError(error: Throwable, context: Map<String, String> = emptyMap()) {
    Log.e("ErrorHandler", "Error: ${error.message}", error)
    
    // Optional: Send to Crashlytics
    // FirebaseCrashlytics.getInstance().apply {
    //     context.forEach { (key, value) -> setCustomKey(key, value) }
    //     recordException(error)
    // }
}
```

**Результат:** Мониторинг ошибок в production (опционально)

---

### Проблема 14: Дублирование кода в Receivers

**Цель:** Рефакторинг общей логики в отдельный класс  
**Файлы:**
- Создать `util/ReminderScheduler.kt`
- `receiver/AlarmReceiver.kt`
- `receiver/BootReceiver.kt`

**Метод:**
1. Вынести общую логику планирования в ReminderScheduler
2. Использовать в обоих receivers

**Создать ReminderScheduler.kt:**
```kotlin
object ReminderScheduler {
    suspend fun rescheduleTaskReminder(context: Context, taskId: Int) {
        try {
            val taskDao = ServiceLocator.getDatabase().taskDao()
            val task = taskDao.getTaskById(taskId) ?: return
            
            if (task.reminderEnabled && !task.isHabit && task.reminderTime != null) {
                val (hour, minute) = AlarmManagerHelper.parseReminderTime(task.reminderTime)
                AlarmManagerHelper.scheduleReminder(
                    context = context,
                    taskId = task.id,
                    title = task.title,
                    hour = hour,
                    minute = minute
                )
                Log.i("ReminderScheduler", "Rescheduled reminder for task ${task.id}")
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to reschedule reminder for task $taskId", e)
        }
    }
    
    suspend fun rescheduleAllReminders(context: Context) {
        try {
            val taskDao = ServiceLocator.getDatabase().taskDao()
            val tasksWithReminders = taskDao.getAllTasks().filter { 
                it.reminderEnabled && !it.isHabit 
            }
            
            tasksWithReminders.forEach { task ->
                rescheduleTaskReminder(context, task.id)
            }
            
            Log.i("ReminderScheduler", "Rescheduled ${tasksWithReminders.size} reminders")
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to reschedule reminders", e)
        }
    }
}
```

**Обновить AlarmReceiver.kt и BootReceiver.kt для использования ReminderScheduler**


**Результат:** DRY принцип, упрощение поддержки

---

# 5. TESTING AND VALIDATION

## Критерии успешного исправления проблем:

### После Фазы 1 (Критические проблемы):
**Обязательные проверки:**
1. ✅ Приложение собирается без ошибок (`./gradlew assembleDebug`)
2. ✅ Приложение запускается без крашей
3. ✅ Нет утечек памяти (проверить через Android Profiler)
4. ✅ Ошибки отображаются пользователю через Snackbar
5. ✅ BroadcastReceivers корректно завершают работу

**Тестовые сценарии:**
- Создать задачу с напоминанием → проверить срабатывание
- Перезагрузить устройство → проверить восстановление напоминаний
- Вызвать ошибку (отключить БД) → проверить отображение Snackbar
- Проверить через LeakCanary отсутствие утечек

**Критерий успеха:** Все критические проблемы устранены, приложение стабильно работает

---

### После Фазы 2 (Функциональные улучшения):
**Обязательные проверки:**
1. ✅ Release сборка работает корректно (`./gradlew assembleRelease`)
2. ✅ ProGuard не ломает функциональность
3. ✅ Разрешение SCHEDULE_EXACT_ALARM запрашивается через UI
4. ✅ Unit-тесты проходят (`./gradlew test`)
5. ✅ cancelAllReminders() работает быстро (< 1 сек)
6. ✅ Запросы с reminderEnabled выполняются быстро

**Тестовые сценарии:**
- Создать 50 задач с напоминаниями → отменить все → проверить время выполнения
- Собрать release APK → установить → проверить все функции
- На Android 12+ попытаться создать напоминание → проверить диалог разрешения
- Запустить unit-тесты → проверить покрытие Use Cases

**Критерий успеха:** Приложение готово к production релизу

---

### После Фазы 3 (Оптимизации):
**Обязательные проверки:**
1. ✅ Все строки вынесены в strings.xml
2. ✅ Нет переполнения XP/монет при экстремальных значениях
3. ✅ Compose UI работает плавно (60 FPS)
4. ✅ Нет дублирования кода в Receivers
5. ✅ (Опционально) Crashlytics интегрирован

**Тестовые сценарии:**
- Прокрутить список из 100 задач → проверить FPS через GPU Profiler
- Попытаться получить 999,999 XP → проверить cap
- Проверить локализацию (переключить язык)
- Проверить отправку ошибок в Crashlytics (если включено)

**Критерий успеха:** Код оптимизирован, maintainability улучшен

---

## Общие метрики качества:

### Производительность:
- Время запуска приложения: < 2 сек
- Время открытия экрана: < 500 мс
- FPS при прокрутке списков: 60 FPS
- Потребление памяти: < 100 MB

### Стабильность:
- Crash-free rate: > 99%
- ANR rate: < 0.1%
- Утечки памяти: 0

### Качество кода:
- Покрытие тестами Use Cases: > 80%
- Количество TODO/FIXME: 0
- Hardcoded strings: 0
- Дублирование кода: минимальное

---

## Финальная валидация перед релизом:

**Чек-лист:**
- [ ] Все 14 проблем исправлены
- [ ] Приложение протестировано на Android 7.0 (API 24)
- [ ] Приложение протестировано на Android 12 (API 31)
- [ ] Приложение протестировано на Android 14 (API 34)
- [ ] Release APK собирается и работает
- [ ] ProGuard rules корректны
- [ ] Разрешения запрашиваются правильно
- [ ] Напоминания работают после перезагрузки
- [ ] Нет утечек памяти
- [ ] Unit-тесты проходят
- [ ] Документация обновлена (README.md)

**Ожидаемый результат:**
Стабильное, производительное и готовое к релизу приложение FoxTask с устраненными критическими проблемами, функциональными улучшениями и оптимизациями кода.
