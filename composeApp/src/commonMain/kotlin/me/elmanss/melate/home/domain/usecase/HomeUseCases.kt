package me.elmanss.melate.home.domain.usecase

data class HomeUseCases(
  val fetchSorteos: FetchSorteos,
  val saveToFavorites: SaveToFavorites,
  val getListId: GetListId,
)
