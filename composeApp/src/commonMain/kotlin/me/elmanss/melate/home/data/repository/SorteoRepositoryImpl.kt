package me.elmanss.melate.home.data.repository

import me.elmanss.melate.home.data.remote.SorteoApi
import me.elmanss.melate.home.domain.repository.SorteoRepository

class SorteoRepositoryImpl(private val sorteoApi: SorteoApi) : SorteoRepository {
  override fun fetchSorteos(): List<Int> {
    return sorteoApi.fetchSorteos()
  }
}
