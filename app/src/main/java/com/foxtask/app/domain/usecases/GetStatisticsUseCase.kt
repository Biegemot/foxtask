package com.foxtask.app.domain.usecases

import com.foxtask.app.data.repository.FoxTaskRepository
import com.foxtask.app.domain.models.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetStatisticsUseCase(
    private val repository: FoxTaskRepository
) {
    suspend operator fun invoke(): Statistics = withContext(Dispatchers.IO) {
        repository.getStatistics()
    }
}
