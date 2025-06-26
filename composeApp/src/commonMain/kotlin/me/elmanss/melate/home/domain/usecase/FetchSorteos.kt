package me.elmanss.melate.home.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.elmanss.melate.home.domain.model.SorteoModel
import me.elmanss.melate.home.domain.repository.SorteoRepository

class FetchSorteos(private val repository: SorteoRepository) {
  operator fun invoke(): Flow<List<SorteoModel>> {
    val sorteos = mutableListOf<List<Int>>()
    repeat(30) { sorteos.add(repository.fetchSorteos()) }

    return flowOf(sorteos).map { it.map { SorteoModel(it) } }
  }
}
