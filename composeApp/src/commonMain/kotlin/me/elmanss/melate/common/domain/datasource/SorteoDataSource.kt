package me.elmanss.melate.common.domain.datasource

import me.elmanss.melate.common.domain.model.RequestState

interface SorteoDataSource {
  suspend fun fetchSorteos(): RequestState<List<Int>>
}