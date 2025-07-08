package me.elmanss.melate.favorites.domain.usecase

data class FavoritesUseCases(
    val addFavorite: AddFavorite,
    val deleteFavorite: DeleteFavorite,
    val fetchFavorites: FetchFavorites,
    val formatFavoriteCreationDate: FormatFavCreationDate,
    val fetchSorteoFromNetwork: FetchSorteoFromNetwork,
)
