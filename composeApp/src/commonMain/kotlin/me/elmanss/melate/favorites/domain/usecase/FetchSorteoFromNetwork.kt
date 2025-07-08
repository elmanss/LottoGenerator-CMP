package me.elmanss.melate.favorites.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.elmanss.melate.common.data.local.FavOrigin
import me.elmanss.melate.common.domain.model.RequestState
import me.elmanss.melate.favorites.domain.model.FavoritoModel
import me.elmanss.melate.home.domain.repository.SorteoRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FetchSorteoFromNetwork(
    private val sorteoRepository: SorteoRepository,
) {
  @OptIn(ExperimentalTime::class)
  suspend operator fun invoke(): Flow<RequestState<FavoritoModel>> {

    val result = sorteoRepository.fetchSorteos()

    return if (result.isSuccess()) {
      flowOf(RequestState.Success(result.getSuccessData().sorted().toFavorito()))
    } else {
      flowOf(RequestState.Error(result.getErrorMessage()))
    }
  }

  @OptIn(ExperimentalTime::class)
  private fun List<Int>.toFavorito() =
      FavoritoModel(
          id = 0L,
          sorteo = this.joinToString(),
          origin = FavOrigin.Network,
          createdAt = Clock.System.now().toEpochMilliseconds(),
          selected = false,
      )
}
