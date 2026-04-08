# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 📱 Обзор проекта
FoxTask — мобильное приложение для управления задачами и привычками с геймификацией, построено на современном Android-стеке: Kotlin 100%, Jetpack Compose + Material 3, Room для локальной БД, Coroutines + Flow, AlarmManager для точных напоминаний.

## 🔨 Основные команды разработки

### Сборка и запуск
```bash
# Сборка debug APK
./gradlew assembleDebug

# Установка на устройство/эмулятор
adb install app/build/outputs/apk/debug/app-debug.apk

# Очистка проекта
./gradlew clean

# Проверка зависимостей
./gradlew dependencies

# Запуск лinters (если настроены)
./gradlew lint
```

### Тестирование
```bash
# Запуск unit-тестов
./gradlew test

# Запуск instrumented тестов
./gradlew connectedAndroidTest

# Запуск конкретного теста
./gradlew test --tests "com.example.MyTest.testMethod"
```

## 🏗️ Архитектура приложения

### Структура пакетов
```
app/
├── data/
│   ├── local/              # Слой данных (Room)
│   │   ├── dao/            # DAO интерфейсы для доступа к БД
│   │   ├── entities/       # Room сущности (Task, User, Item, etc.)
│   │   └── FoxTaskDatabase.kt  # Room база данных
│   ├── models/             # Модели данных для представления
│   └── repository/         # Репозитории (и реализации)
├── presentation/           # Презентационный слой (MVVM)
│   ├── viewmodel/          # ViewModels (TasksViewModel, ShopViewModel, etc.)
│   ├── ui/
│   │   ├── screens/        # Composables экранов (TasksScreen, ShopScreen, etc.)
│   │   ├── components/     # Повторно используемые компоненты (TaskCard, ItemCard, etc.)
│   │   └── theme/          # Material 3 тема (цвета, типыографика, формы)
│   └── navigation/         # Навигация (NavGraph, Screen сеалы)
├── util/                   # Вспомогательные утилиты
│   ├── AlarmManagerHelper.kt  # Точное планирование напоминаний
│   └── NotificationHelper.kt  # Работа с уведомлениями
└── receiver/               # Broadcast ресиверы
    ├── AlarmReceiver.kt    # Обработчик срабатывания будильников
    └── BootReceiver.kt     # Восстановление будильников после перезагрузки
```

### Ключевые технические решения
- **Room Database**: Версия 2 с миграцией MIGRATION_1_2 для добавления индексов
- **Jetpack Compose**: Declarative UI с Material 3
- **State Management**: ViewModels + StateFlow/LiveData для управления UI-состоянием
- **Напоминания**: AlarmManager вместо WorkManager для точного времени срабатывания
- **Dependency Injection**: Простой ServiceLocator для доступа к зависимостям
- **Кастомизация**: Векторные ресурсы для предметов гардероба (36+ предметов)

### Важные файлы для понимания
- `FoxTaskDatabase.kt` - определение Room базы данных со всеми сущностями и DAO
- `FoxTaskApplication.kt` - инициализация зависимостей при запуске приложения
- `NavGraph.kt` - основная навигация между экранами
- `AlarmManagerHelper.kt` - ядро функционала точных напоминаний
- `FoxTaskRepositoryImpl.kt` - реализация репозитория с бизнес-логикой

## 💡 Особенности разработки

### Работа с БД
Все DAO интерфейсы используют suspend функции для корoutines. При добавлении новой сущности:
1. Добавьте entity класс в `data/local/entities/`
2. Добавьте DAO интерфейс в `data/local/dao/`
3. Обновите `@Database` аннотацию в `FoxTaskDatabase.kt`
4. При изменении версии БД добавьте миграцию в `FoxTaskDatabase.kt`

### Работа с напоминаниями
- Напоминания хранятся в сущности Task (поле reminderEnabled + reminderTime)
- Для планирования используется AlarmManagerHelper
- При срабатывании AlarmReceiver создает уведомление и перепланирует ежедневные напоминания
- BootReceiver восстанавливает все активные напоминания после перезагрузки устройства

### Темизация
Цвета и стили определены в `presentation/ui/theme/`:
- `Color.kt` - палитра цветов (включая цвета приоритетов задач)
- `Type.kt` - типыографика
- `Shape.kt` - формы компонентов

## ⚠️ Известные ограничения
- Требуется явное разрешение `SCHEDULE_EXACT_ALARM` на Android 12+
- Нет unit-тестов в текущей реализации
- Некоторые строки не вынесены в ресурсы для локализации

## 📝 Рекомендации по вкладкам
При добавлении новой функциональности:
1. Следуйте существующим паттернам именования и структуры пакетов
2. Используйте Kotlin корoutines для асинхронных операций
3. Для UI-компонентов следуйте principles of Jetpack Compose (однонаправленный поток данных)
4. При работе с БД всегда используйте `@Transaction` для сложных операций
5. Добавляйте unit-тесты для новой бизнес-логики в соответствующие test-папки