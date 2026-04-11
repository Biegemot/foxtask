package com.foxtask.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.foxtask.app.data.local.Converters
import com.foxtask.app.data.local.dao.HabitProgressDao
import com.foxtask.app.data.local.dao.InventoryDao
import com.foxtask.app.data.local.dao.ItemDao
import com.foxtask.app.data.local.dao.OutfitDao
import com.foxtask.app.data.local.dao.TaskDao
import com.foxtask.app.data.local.dao.UserDao
import com.foxtask.app.data.local.entities.HabitProgress
import com.foxtask.app.data.local.entities.Inventory
import com.foxtask.app.data.local.entities.Item
import com.foxtask.app.data.local.entities.Outfit
import com.foxtask.app.data.local.entities.Task
import com.foxtask.app.data.local.entities.User

@Database(
    entities = [
        User::class,
        Item::class,
        Inventory::class,
        Outfit::class,
        Task::class,
        HabitProgress::class
    ],
    version = 3,
    exportSchema = false
)
@androidx.room.TypeConverters(Converters::class)
abstract class FoxTaskDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun outfitDao(): OutfitDao
    abstract fun taskDao(): TaskDao
    abstract fun habitProgressDao(): HabitProgressDao

    companion object {
        @Volatile
        private var INSTANCE: FoxTaskDatabase? = null

        fun getInstance(context: Context): FoxTaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoxTaskDatabase::class.java,
                    "foxtask_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // dev convenience
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Индексы для tasks
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_isHabit ON tasks(isHabit)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_isCompleted ON tasks(isCompleted)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_isHabit_isCompleted ON tasks(isHabit, isCompleted)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_dueDate ON tasks(dueDate)")
                // Индексы для items
                database.execSQL("CREATE INDEX IF NOT EXISTS index_items_category ON items(category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_items_tier ON items(tier)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_items_isActive ON items(isActive)")
                // Индексы для inventory
                database.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_userId ON inventory(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_userId_itemId ON inventory(userId, itemId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_inventory_isEquipped ON inventory(isEquipped)")
            }
        }
        
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Добавить индекс для reminderEnabled для оптимизации запросов напоминаний
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_reminderEnabled ON tasks(reminderEnabled)")
            }
        }
    }
}
