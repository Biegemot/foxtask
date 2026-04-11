package com.foxtask.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.foxtask.app.data.local.entities.Outfit

import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    @Query("SELECT * FROM outfit WHERE userId = :userId LIMIT 1")
    suspend fun getOutfit(userId: Int): Outfit?

    @Query("SELECT * FROM outfit WHERE userId = :userId LIMIT 1")
    fun getOutfitStream(userId: Int): Flow<Outfit?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfit(outfit: Outfit)

    @Update
    suspend fun updateOutfit(outfit: Outfit)

    @Query("UPDATE outfit SET hatItemId = :itemId WHERE userId = :userId")
    suspend fun setHatItem(userId: Int, itemId: Int?)

    @Query("UPDATE outfit SET glassesItemId = :itemId WHERE userId = :userId")
    suspend fun setGlassesItem(userId: Int, itemId: Int?)

    @Query("UPDATE outfit SET scarfItemId = :itemId WHERE userId = :userId")
    suspend fun setScarfItem(userId: Int, itemId: Int?)

    @Query("UPDATE outfit SET cloakItemId = :itemId WHERE userId = :userId")
    suspend fun setCloakItem(userId: Int, itemId: Int?)

    @Query("UPDATE outfit SET furColorItemId = :itemId WHERE userId = :userId")
    suspend fun setFurColorItem(userId: Int, itemId: Int?)

    @Query("UPDATE outfit SET backgroundThemeId = :itemId WHERE userId = :userId")
    suspend fun setBackgroundThemeItem(userId: Int, itemId: Int?)

    @Query("UPDATE outfit SET maskItemId = :itemId WHERE userId = :userId")
    suspend fun setMaskItem(userId: Int, itemId: Int?)

    @Query("UPDATE outfit SET bandanaItemId = :itemId WHERE userId = :userId")
    suspend fun setBandanaItem(userId: Int, itemId: Int?)

    @Query("UPDATE outfit SET maoriPatternItemId = :itemId WHERE userId = :userId")
    suspend fun setMaoriPatternItem(userId: Int, itemId: Int?)
}
