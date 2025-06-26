package me.elmanss.melate.favorites.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.elmanss.melate.favorites.domain.model.FavoritoModel
import me.elmanss.melate.favorites.domain.usecase.FavoritesUseCases

sealed class ListFavUiEvent {
  data object FetchFavs : ListFavUiEvent()

  data class ShowDeleteFavDialog(val fav: FavoritoModel) : ListFavUiEvent()

  data class DeleteFav(val fav: FavoritoModel) : ListFavUiEvent()

  data object HideDeleteFavDialog : ListFavUiEvent()

  data class EnableMultiDelete(val fav: FavoritoModel, val index: Int) : ListFavUiEvent()

  data object DisableMultiDelete : ListFavUiEvent()

  data class SelectFav(val fav: FavoritoModel, val index: Int) : ListFavUiEvent()

  data object ShowMultiDeleteFavDialog : ListFavUiEvent()

  data object HideMultiDeleteFavDialog : ListFavUiEvent()

  data object GoToCreate : ListFavUiEvent()

  data object ClearFlags : ListFavUiEvent()

  data object DeleteMultipleFavs : ListFavUiEvent()

  data object NavigateBack : ListFavUiEvent()

  data object HideSuccessMessage : ListFavUiEvent()
}

class ListFavoritesScreenViewModel(private val useCases: FavoritesUseCases) : ScreenModel {
  private val _state = MutableStateFlow(ListFavoritesScreenState())
  val state =
      _state
          .asStateFlow()
          .stateIn(screenModelScope, SharingStarted.Eagerly, ListFavoritesScreenState())

  private var fetchJob: Job? = null

  init {
    launchFetchFavorites()
  }

  fun sendEvent(event: ListFavUiEvent) {
    when (event) {
      ListFavUiEvent.ClearFlags -> {
        _state.update { state -> state.clearFlags() }
      }
      is ListFavUiEvent.DeleteFav -> {
        deleteFavs(event.fav)
      }
      ListFavUiEvent.DisableMultiDelete -> {
        clearSelected()
      }
      is ListFavUiEvent.EnableMultiDelete -> {
        markItemAsSelected(event.fav, event.index)
        _state.update { state -> state.copy(multiselectEnabled = true) }
      }
      ListFavUiEvent.FetchFavs -> {
        launchFetchFavorites()
      }
      ListFavUiEvent.GoToCreate -> {
        _state.update { state -> state.copy(favTapped = true) }
      }
      ListFavUiEvent.HideDeleteFavDialog -> {
        showWarning()
      }
      is ListFavUiEvent.SelectFav -> {
        markItemAsSelected(event.fav, event.index)
      }
      is ListFavUiEvent.ShowDeleteFavDialog -> {
        showWarning(event.fav)
      }
      is ListFavUiEvent.ShowMultiDeleteFavDialog -> {
        showMultideletionPrompt(true)
      }
      is ListFavUiEvent.HideMultiDeleteFavDialog -> {
        showMultideletionPrompt(false)
      }
      ListFavUiEvent.DeleteMultipleFavs -> {
        deleteSelected()
      }
      ListFavUiEvent.HideSuccessMessage -> {
        showDeletionMessage(false)
      }

      ListFavUiEvent.NavigateBack -> _state.update { state -> state.copy(navigateBack = true) }
    }
  }

  private fun deleteFavs(model: FavoritoModel) {
    screenModelScope.launch {
      useCases.deleteFavorite(model)
      delay(250)
      dismissWarning()
      showDeletionMessage(true)
    }
  }

  private fun launchFetchFavorites() {
    fetchJob?.cancel()
    fetchJob =
        useCases
            .fetchFavorites()
            .map { it }
            .onEach { _state.update { state -> state.copy(favs = it) } }
            .launchIn(screenModelScope)
  }

  private fun showWarning(sorteo: FavoritoModel? = null) {
    Logger.d { "clicked fav" }
    _state.update { state -> state.copy(favToDelete = sorteo) }
  }

  private fun dismissWarning() {
    _state.update { state -> state.copy(favToDelete = null) }
  }

  private fun showDeletionMessage(show: Boolean = false) {
    _state.update { state -> state.copy(showDeletionSuccess = show) }
  }

  fun formatDate(favModel: FavoritoModel) =
      useCases.formatFavoriteCreationDate.invoke(favModel.createdAt)

  private fun markItemAsSelected(fav: FavoritoModel, index: Int) {
    val currentFavsMutable = state.value.favs.toMutableList()
    Logger.d { "Current favs: ${currentFavsMutable}" }
    Logger.d { "Tapped index: $index" }

    currentFavsMutable[index] = fav
    _state.update { state -> state.copy(favs = currentFavsMutable) }
  }

  private fun deleteSelected() {
    screenModelScope.launch {
      val selectedFavs = state.value.favs.filter { it.selected }
      Logger.d { "Selected favs: $selectedFavs" }
      selectedFavs
          .forEach { useCases.deleteFavorite(it) }
          .also {
            clearSelected()
            _state.update { state -> state.copy(multideleteCompleted = true) }
          }
    }
  }

  private fun clearSelected() {
    val clearedFavs = state.value.favs.onEach { it.selected = false }
    Logger.d { "Cleared favs: ${clearedFavs}" }
    _state.update { state -> state.copy(favs = clearedFavs, multiselectEnabled = false) }
  }

  private fun showMultideletionPrompt(show: Boolean = false) {
    _state.update { state -> state.copy(showMultiDeletionPrompt = show) }
  }
}
