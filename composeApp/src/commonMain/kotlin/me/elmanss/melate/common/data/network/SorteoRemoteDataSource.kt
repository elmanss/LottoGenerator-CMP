package me.elmanss.melate.common.data.network

import co.touchlab.kermit.Logger
import me.elmanss.melate.common.domain.SorteoApi
import me.elmanss.melate.common.domain.datasource.SorteoDataSource
import me.elmanss.melate.common.domain.model.RequestState

class SorteoRemoteDataSource(private val api: SorteoApi) : SorteoDataSource {

  override suspend fun fetchSorteos(): RequestState<List<Int>> {
    val response =
        api.fetchSorteos(version = API_VERSION, min = MIN_DRAW, max = MAX_DRAW, count = DRAW_COUNT)
    if (response.isSuccess()) {
      val result = response.getSuccessData()
      Logger.d(TAG) { result.joinToString(separator = "\n") }
      return RequestState.Success(data = result)
    } else {
      return RequestState.Error(response.getErrorMessage())
    }
  }

  companion object {
    private const val TAG = "SorteoRemoteDataSource"
    private const val API_VERSION = "v1.0"
    private const val MIN_DRAW = "1"
    private const val MAX_DRAW = "56"
    private const val DRAW_COUNT = "6"
  }
}
