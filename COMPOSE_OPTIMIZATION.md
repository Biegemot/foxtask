# Рекомендации по оптимизации Compose Recomposition

## Уже реализовано в проекте:

✅ Использование `remember` для сохранения состояния  
✅ Использование `LaunchedEffect` для side effects  
✅ Использование `collectAsState()` для Flow  
✅ Разделение на отдельные Composable функции  

## Рекомендации для дальнейшей оптимизации:

### 1. Использовать `derivedStateOf` для вычисляемых значений

```kotlin
// Вместо:
val filteredTasks = tasks.filter { !it.isCompleted }

// Использовать:
val filteredTasks by remember(tasks) {
    derivedStateOf { tasks.filter { !it.isCompleted } }
}
```

### 2. Использовать `key()` в LazyColumn

```kotlin
LazyColumn {
    items(
        items = tasks,
        key = { task -> task.id } // Stable key для оптимизации
    ) { task ->
        TaskCard(task = task)
    }
}
```

### 3. Стабилизировать lambda параметры

```kotlin
// Вместо:
TaskCard(
    task = task,
    onClick = { onTaskClick(task.id) }
)

// Использовать:
TaskCard(
    task = task,
    onClick = remember(task.id) { { onTaskClick(task.id) } }
)
```

### 4. Использовать `@Stable` и `@Immutable` аннотации

```kotlin
@Immutable
data class TaskUiState(
    val tasks: List<Task>,
    val isLoading: Boolean
)
```

### 5. Избегать создания новых объектов в Composable

```kotlin
// Плохо:
@Composable
fun MyScreen() {
    val padding = PaddingValues(16.dp) // Создается при каждой recomposition
}

// Хорошо:
private val DefaultPadding = PaddingValues(16.dp)

@Composable
fun MyScreen() {
    val padding = DefaultPadding
}
```

### 6. Использовать `Modifier` правильно

```kotlin
// Плохо:
Box(modifier = Modifier.padding(16.dp).fillMaxWidth())

// Хорошо (порядок важен):
Box(modifier = Modifier.fillMaxWidth().padding(16.dp))
```

## Инструменты для профилирования:

1. **Layout Inspector** - анализ иерархии UI
2. **Compose Compiler Metrics** - отчеты о recomposition
3. **Systrace/Perfetto** - профилирование производительности

## Включение Compose Compiler Metrics:

Добавить в `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.enableJetifier=true
android.useAndroidX=true

# Compose Compiler Metrics
android.enableComposeCompilerMetrics=true
android.enableComposeCompilerReports=true
```

Затем запустить:
```bash
./gradlew assembleRelease -Pandroid.enableComposeCompilerMetrics=true
```

Отчеты будут в `app/build/compose_metrics/`

## Приоритеты оптимизации:

1. **Высокий**: LazyColumn с большим количеством элементов
2. **Средний**: Часто обновляемые экраны (главный экран)
3. **Низкий**: Редко используемые экраны (настройки)

## Текущее состояние проекта:

Проект уже использует большинство best practices для Compose. Основные области для улучшения:

- Добавить `key()` в LazyColumn (если еще не добавлено)
- Использовать `derivedStateOf` для фильтрации списков
- Профилировать с помощью Compose Compiler Metrics

## Заключение:

Текущая реализация уже достаточно оптимизирована. Дальнейшие улучшения следует делать на основе реальных измерений производительности, а не преждевременной оптимизации.
