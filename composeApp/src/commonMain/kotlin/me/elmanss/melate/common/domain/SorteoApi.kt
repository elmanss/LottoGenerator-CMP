package me.elmanss.melate.common.domain

import me.elmanss.melate.common.domain.model.RequestState

interface SorteoApi {
  companion object {
    val URL = "https://www.randomnumberapi.com/api/"
  }

  suspend fun fetchSorteos(
      version: String,
      min: String,
      max: String,
      count: String,
  ): RequestState<List<Int>>
}
