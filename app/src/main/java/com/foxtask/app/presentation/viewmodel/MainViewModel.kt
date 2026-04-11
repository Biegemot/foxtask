package com.foxtask.app.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.foxtask.app.data.local.entities.Item
import com.foxtask.app.data.local.entities.Outfit
import com.foxtask.app.data.models.FoxOutfit
import com.foxtask.app.data.repository.FoxTaskRepository
import com.foxtask.app.domain.usecases.CalculateLevelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = com.foxtask.app.di.ServiceLocator.getRepository()
    private val calculateLevelUseCase = com.foxtask.app.di.ServiceLocator.getCalculateLevelUseCase()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getUserStream().collect { user ->
                if (user != null) {
                    val (level, xpToNext) = calculateLevelUseCase(user.currentXp)
                    _uiState.value = _uiState.value.copy(
                        level = level,
                        currentXp = user.currentXp,
                        coins = user.coins,
                        xpToNextLevel = xpToNext,
                        totalXpForNextLevel = 100 * (level + 1) * (level + 1)
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.getOutfitStream(1).collect { outfitEntity ->
                if (outfitEntity != null) {
                    val outfit = FoxOutfit(
                        hatItemId = outfitEntity.hatItemId,
                        glassesItemId = outfitEntity.glassesItemId,
                        maskItemId = outfitEntity.maskItemId,
                        scarfItemId = outfitEntity.scarfItemId,
                        bandanaItemId = outfitEntity.bandanaItemId,
                        cloakItemId = outfitEntity.cloakItemId,
                        furColorItemId = outfitEntity.furColorItemId,
                        backgroundThemeId = outfitEntity.backgroundThemeId,
                        maoriPatternItemId = outfitEntity.maoriPatternItemId
                    )
                    _uiState.value = _uiState.value.copy(outfit = outfit)
                }
            }
        }
        loadItems()
    }

    fun refresh() {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            val items = repository.getAllActiveItems()
            _uiState.value = _uiState.value.copy(itemsMap = items.associateBy { it.id })
        }
    }

    data class MainUiState(
        val level: Int = 1,
        val currentXp: Int = 0,
        val coins: Int = 0,
        val xpToNextLevel: Int = 100,
        val totalXpForNextLevel: Int = 400,
        val outfit: FoxOutfit = FoxOutfit(),
        val itemsMap: Map<Int, com.foxtask.app.data.local.entities.Item> = emptyMap(),
        val showLevelUpAnimation: Boolean = false,
        val newLevel: Int = 0
    )
}
