# FoxTask

**FoxTask** — это мобильное приложение для управления задачами и привычками с геймификацией, напоминаниями и системой кастомизации.

---

## ✨ Особенности

- 📝 **Задачи и привычки** — создавайте повторяющиеся привычки и разовые задачи с XP и наградами
- ⏰ **Точные напоминания** — использование `AlarmManager` для срабатывания в заданное время (без ограничений WorkManager)
- 🎮 **Геймификация** — система уровней, опыта (XP) и монет
- 👕 **Гардероб** — 36+ векторных предметов для кастомизации лиса-аватара (шейные уборы, очки, маски, шарфы, плащи, цвета меха, фоны, узоры Маори)
- 📊 **Статистика** — детальная аналитика с графиками (Vico)
- 🔄 **Автоматическая переустановка напоминаний** — после перезагрузки устройства будильники восстанавливаются автоматически через `BootReceiver`

---

## 📱 Требования

- **Android 7.0 (API 24)** и выше
- **Kotlin** + **Jetpack Compose**
- **Room** для локальной БД
- **AlarmManager** для точных напоминаний
- **Gradle 8.4** (для локальной сборки)

---

## 🛠️ Локальная сборка

Проект использует Gradle wrapper (не включён в репозиторий). Для локальной сборки:

```bash
# Установите Gradle 8.4 (например, через SDKMAN)
sdk install gradle 8.4
# илиbrew install gradle

# Сгенерируйте Gradle wrapper
gradle wrapper --gradle-version 8.4

# Соберите debug APK
./gradlew assembleDebug

# Или используйте system Gradle напрямую
gradle assembleDebug
```

**Примечание:** GitHub Actions использует system Gradle (устанавливается в workflow), поэтому wrapper не требуется для CI.

---

## 🏗️ Архитектура

```
app/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAO (Task, HabitProgress, Inventory, Item, Outfit)
│   │   ├── entities/     # Сущности БД
│   │   └── FoxTaskDatabase.kt  # База данных v2
│   └── repository/
│       └── impl/FoxTaskRepositoryImpl.kt
├── domain/
│   ├── models/
│   │   └── TaskPriority.kt   # 5 уровней (MINIMAL..CRITICAL)
│   └── usecases/         # Use Cases (CompleteTask, EquipItem, PurchaseItem)
├── presentation/
│   ├── viewmodel/        # ViewModels (Tasks, Shop, Wardrobe, Stats)
│   ├── ui/
│   │   ├── screens/      # Экраны (Tasks, Shop, Wardrobe, Stats, TaskEdit)
│   │   ├── components/   # ItemCard, TaskCard, FoxCharacter, XpProgressBar
│   │   └── theme/        # Material 3 тема, цвета приоритетов
│   └── navigation/       # NavGraph
├── util/
│   ├── AlarmManagerHelper.kt  # Точные будильники
│   ├── NotificationHelper.kt  # Уведомления
│   └── WorkManagerScheduler.kt (deprecated)
└── receiver/
    ├── AlarmReceiver.kt   # Обработчик срабатывания будильников
    └── BootReceiver.kt   # Переустановка после reboot
```

---

## 🔧 Сборка и запуск

### 1. Клонируйте репозиторий
```bash
git clone <repository-url>
cd FoxTask
```

### 2. Откройте в Android Studio
- **File → Open** → выберите папку `FoxTask`
- Дождитесь завершения синхронизации Gradle

### 3. Соберите проект
```bash
./gradlew assembleDebug
```
Или через Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

### 4. Установите на устройство/эмулятор
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎯 Основные возможности

### Напоминания
- Задачи → edited → "Напоминание" → время → `AlarmManager` планирует точное срабатывание
- При срабатывании: `AlarmReceiver` показывает уведомление и **автоматически перезапланирует на следующий день** (для ежедневных напоминаний)
- После перезагрузки: `BootReceiver` читает все задачи с `reminderEnabled` и восстанавливает будильники

### Кастомизация
- Категории предметов: `HAT`, `GLASSES`, `MASK`, `SCARF`, `BANDANA`, `CLOAK`, `FUR_COLOR`, `BACKGROUND`, `MAORI_PATTERN`
- Тiers: `COMMON` (серый), `RARE` (синий), `EPIC` (фиолетовый) с премиум-бейджем "P"
- Векторные ресурсы: 39 файлов в `res/drawable/` (формат `ic_<category>_<id>.xml`)

### Статистика
- Выполненные задачи и привычки
- Общий XP и монеты
- Текущий и максимальный стрик
- Процент завершения за неделю/месяц (графики Vico)

---

## 🔐 Разрешения

- `POST_NOTIFICATIONS` — показ уведомлений о напоминаниях
- `VIBRATE` — вибрация
- `SCHEDULE_EXACT_ALARM` — точные будильники (требуется для AlarmManager)
- `RECEIVE_BOOT_COMPLETED` — переустановка будильников после перезагрузки

---

## 📄 Лицензия

MIT License. См. файл [LICENSE](LICENSE).

---

## 👨‍💻 Разработка

Проект использует modern Android-стек:
- **Kotlin** 100%
- **Jetpack Compose** + **Material 3**
- **Room** с `@Transaction` для устранения N+1
- **Coroutines** + **Flow**
- **Navigation Compose**

### Ключевые исправления (финальный аудит)
- ✅ `InventoryWithItem` с `@Relation` (без N+1)
- ✅ `TaskPriority` 5 уровней + цвета
- ✅ Векторные ресурсы для всех предметов
- ✅ AlarmManager вместо WorkManager
- ✅ Индексы в миграции `MIGRATION_1_2`
- ✅ Глобальный `CoroutineExceptionHandler`

---

## 📸 Скриншоты

*(добавить после сборки)*

---

## 🐛 Известные ограничения

- `SCHEDULE_EXACT_ALARM` на Android 12+ требует явного подтверждения пользователем (системное диалоговое окно)
- `cancelAllReminders()` перебирает ID 1..1000 — неоптимально, но не используется в production
- Нет Unit-тестов (планируется)

---

**Статус:** ✅ Готов к релизу MVP.
