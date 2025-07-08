package me.elmanss.melate.home.data.repository

import me.elmanss.melate.common.domain.datasource.SorteoDataSource
import me.elmanss.melate.common.domain.model.RequestState
import me.elmanss.melate.home.domain.repository.SorteoRepository

class SorteoRepositoryImpl(private val sorteoApi: SorteoDataSource) : SorteoRepository {
  override suspend fun fetchSorteos(): RequestState<List<Int>> {
    return sorteoApi.fetchSorteos()
  }
}
