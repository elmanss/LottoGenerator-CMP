package me.elmanss.melate.favorites.presentation

import me.elmanss.melate.favorites.domain.model.FavoritoModel

data class ListFavoritesScreenState(
    val favs: List<FavoritoModel> = emptyList(),
    val showDeletionSuccess: Boolean = false,
    val favToDelete: FavoritoModel? = null,
    val showMultiDeletionPrompt: Boolean = false,
    val multiselectEnabled: Boolean = false,
    val multideleteCompleted: Boolean = false,
    val favTapped: Boolean = false,
    val navigateBack: Boolean = false,
) {
  fun clearFlags() =
      this.copy(
          showDeletionSuccess = false,
          favToDelete = null,
          showMultiDeletionPrompt = false,
          multiselectEnabled = false,
          multideleteCompleted = false,
          favTapped = false,
          navigateBack = false)
}
