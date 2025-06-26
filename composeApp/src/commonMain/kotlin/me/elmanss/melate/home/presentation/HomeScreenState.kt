package me.elmanss.melate.home.presentation

import me.elmanss.melate.home.domain.model.SorteoModel

data class HomeScreenState(
  val sorteos: List<SorteoModel> = emptyList(),
  val isWarningShown: Boolean = false,
  val clickedSorteo: SorteoModel? = null,
  val showStorageSuccess: Boolean = false,
  val onGoToFav: Boolean = false,
  val multiSelectMode: Boolean = false,
) {
  fun clearFlags() =
    this.copy(
      isWarningShown = false,
      clickedSorteo = null,
      showStorageSuccess = false,
      onGoToFav = false,
      multiSelectMode = false,
    )
}
