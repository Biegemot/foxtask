package com.foxtask.app.di

import android.content.Context
import android.util.Log
import com.foxtask.app.data.local.*
import com.foxtask.app.data.local.dao.*
import com.foxtask.app.data.local.entities.*
import com.foxtask.app.data.models.*
import com.foxtask.app.data.repository.FoxTaskRepository
import com.foxtask.app.data.repository.impl.FoxTaskRepositoryImpl
import com.foxtask.app.domain.usecases.*
import com.foxtask.app.presentation.viewmodel.*
import com.foxtask.app.util.ErrorHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ServiceLocator {
    private var database: FoxTaskDatabase? = null
    private var repository: FoxTaskRepository? = null
    lateinit var globalExceptionHandler: CoroutineExceptionHandler
        private set

    fun init(context: Context) {
        // Initialize global exception handler
        globalExceptionHandler = CoroutineExceptionHandler { _, exception ->
            Log.e("GlobalExceptionHandler", "Unhandled coroutine exception", exception)
            CoroutineScope(Dispatchers.Main).launch {
                ErrorHandler.handleError(exception)
            }
        }

        database = FoxTaskDatabase.getInstance(context)
        val db = database ?: throw IllegalStateException("Database initialization failed")
        repository = FoxTaskRepositoryImpl(
            db.userDao(),
            db.taskDao(),
            db.habitProgressDao(),
            db.itemDao(),
            db.inventoryDao(),
            db.outfitDao()
        )
        // Prepopulate data in background with exception handling
        CoroutineScope(Dispatchers.IO + globalExceptionHandler).launch {
            prepopulateDatabase()
        }
    }

    private suspend fun prepopulateDatabase() {
        val db = database ?: return
        val itemDao = db.itemDao()
        val userDao = db.userDao()
        val outfitDao = db.outfitDao()

        // Items (if empty)
        if (itemDao.getAllActiveItems().isEmpty()) {
            val items = listOf(
                // Hats (4 common)
                Item(1, "Классическая шляпа", "Стильная фетровая шляпа", ItemCategory.HAT, ItemTier.COMMON, 50, "ic_hat_1"),
                Item(2, "Бейсболка", "Удобная бейсболка", ItemCategory.HAT, ItemTier.COMMON, 75, "ic_hat_2"),
                Item(3, "Кепка", "Простая кепка", ItemCategory.HAT, ItemTier.COMMON, 60, "ic_hat_3"),
                Item(4, "Колокольчик", "Веселый колокольчик", ItemCategory.HAT, ItemTier.COMMON, 100, "ic_hat_4"),
                // Hats rare (2)
                Item(5, "Тропический шлем", "Шлем для приключений", ItemCategory.HAT, ItemTier.RARE, 250, "ic_hat_5"),
                Item(6, "Королевская корона", "Блестящая корона", ItemCategory.HAT, ItemTier.RARE, 350, "ic_hat_6"),
                // Hats epic (1)
                Item(7, "Шлем Асгарда", "Мифический шлем", ItemCategory.HAT, ItemTier.EPIC, 800, "ic_hat_7"),

                // Glasses (4 common)
                Item(101, "Простые очки", "Классические очки", ItemCategory.GLASSES, ItemTier.COMMON, 70, "ic_glasses_1"),
                Item(102, "Солнцезащитные", "Удобные очки от солнца", ItemCategory.GLASSES, ItemTier.COMMON, 90, "ic_glasses_2"),
                Item(103, "Очки в полоску", "Модные очки", ItemCategory.GLASSES, ItemTier.COMMON, 80, "ic_glasses_3"),
                Item(104, "Линзы", "Новые линзы", ItemCategory.GLASSES, ItemTier.COMMON, 100, "ic_glasses_4"),
                // Glasses rare (2)
                Item(105, "Циплокера", "Стильные циплокера", ItemCategory.GLASSES, ItemTier.RARE, 280, "ic_glasses_5"),
                Item(106, "Маска", "Загадочная маска", ItemCategory.GLASSES, ItemTier.RARE, 320, "ic_glasses_6"),
                // Glasses epic (1)
                Item(107, "Очки будущего", "Технологичные очки", ItemCategory.GLASSES, ItemTier.EPIC, 700, "ic_glasses_7"),

                // Scarves/Bandanas (4 common)
                Item(201, "Простой шарф", "Теплый шарф", ItemCategory.SCARF, ItemTier.COMMON, 60, "ic_scarf_1"),
                Item(202, "Бандана", "Красная бандана", ItemCategory.BANDANA, ItemTier.COMMON, 50, "ic_bandana_1"),
                Item(203, "Шарф-труба", "Уютный шарф", ItemCategory.SCARF, ItemTier.COMMON, 75, "ic_scarf_2"),
                Item(204, "Шелковый шарф", "Легкий шелковый шарф", ItemCategory.SCARF, ItemTier.COMMON, 90, "ic_scarf_3"),
                // Scarves rare (2)
                Item(205, "Зимняя шапка", "Теплая шапка с шарфом", ItemCategory.SCARF, ItemTier.RARE, 300, "ic_scarf_4"),
                Item(206, "Бандана ниндзя", "Темная бандана", ItemCategory.BANDANA, ItemTier.RARE, 280, "ic_bandana_2"),
                // Cloak epic (1)
                Item(207, "Магический плащ", "Светящийся плащ", ItemCategory.CLOAK, ItemTier.EPIC, 900, "ic_cloak_1"),

                // Fur Colors (4 common)
                Item(301, "Рыжий", "Классический рыжий цвет", ItemCategory.FUR_COLOR, ItemTier.COMMON, 100, "ic_fur_orange"),
                Item(302, "Белый", "Снежно-белый", ItemCategory.FUR_COLOR, ItemTier.COMMON, 120, "ic_fur_white"),
                Item(303, "Седой", "Мудрый седой", ItemCategory.FUR_COLOR, ItemTier.COMMON, 150, "ic_fur_silver"),
                Item(304, "Черный", "Таинственный черный", ItemCategory.FUR_COLOR, ItemTier.COMMON, 180, "ic_fur_black"),
                // Fur colors rare (2)
                Item(305, "Золотой", "Блестящий золотой", ItemCategory.FUR_COLOR, ItemTier.RARE, 350, "ic_fur_gold"),
                Item(306, "Радужный", "Переливающийся радугой", ItemCategory.FUR_COLOR, ItemTier.RARE, 400, "ic_fur_rainbow"),
                // Fur colors epic (1)
                Item(307, "Космический", "Звездный окрас", ItemCategory.FUR_COLOR, ItemTier.EPIC, 950, "ic_fur_cosmic"),

                // Backgrounds (2 common)
                Item(401, "Ночь", "Темный фон", ItemCategory.BACKGROUND, ItemTier.COMMON, 80, "ic_background_1"),
                Item(402, "Закат", "Оранжевый фон", ItemCategory.BACKGROUND, ItemTier.COMMON, 100, "ic_background_2"),

                // Maori patterns (2 common)
                Item(501, "Кору", "Узор волны", ItemCategory.MAORI_PATTERN, ItemTier.COMMON, 120, "ic_maori_1"),
                Item(502, "Хеке", "Узор инициалов", ItemCategory.MAORI_PATTERN, ItemTier.COMMON, 150, "ic_maori_2")
            )
            itemDao.insertItems(*items.toTypedArray())
        }

        // Default user (if none)
        if (userDao.getUser() == null) {
            userDao.insertUser(User(id = 1, level = 1, currentXp = 0, coins = 100))
        }

        // Default outfit (if none)
        if (outfitDao.getOutfit(1) == null) {
            outfitDao.insertOutfit(Outfit(userId = 1, furColorItemId = 301))
        }
    }

    fun getDatabase(): FoxTaskDatabase {
        return database ?: throw IllegalStateException("Database not initialized")
    }

    fun getRepository(): FoxTaskRepository {
        return repository ?: throw IllegalStateException("Repository not initialized")
    }

    fun getCompleteTaskUseCase(): CompleteTaskUseCase {
        return CompleteTaskUseCase(getRepository(), getCalculateLevelUseCase(), getCalculateXpRewardUseCase())
    }

    fun getCompleteHabitUseCase(): CompleteHabitUseCase {
        return CompleteHabitUseCase(getRepository(), getCalculateLevelUseCase())
    }

    fun getPurchaseItemUseCase(): PurchaseItemUseCase {
        return PurchaseItemUseCase(getRepository())
    }

    fun getEquipItemUseCase(): EquipItemUseCase {
        return EquipItemUseCase(getRepository())
    }

    fun getCalculateLevelUseCase(): CalculateLevelUseCase {
        return CalculateLevelUseCase()
    }

    fun getCalculateXpRewardUseCase(): CalculateXpRewardUseCase {
        return CalculateXpRewardUseCase()
    }

    fun getGetStatisticsUseCase(): GetStatisticsUseCase {
        return GetStatisticsUseCase(getRepository())
    }
}

