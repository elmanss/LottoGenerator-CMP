package me.elmanss.melate.create.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.elmanss.melate.common.data.local.FavOrigin
import me.elmanss.melate.common.util.isDigitsOnly
import me.elmanss.melate.common.util.prettyPrint
import me.elmanss.melate.favorites.domain.model.FavoritoModel
import me.elmanss.melate.favorites.domain.usecase.FavoritesUseCases
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class Clearable {
  BACK_NAVIGATION,
  SORTEO_COMPLETED,
  CAPTURE_NUMBER,
  ERROR,
  AFTER_STORAGE,
  MESSAGE,
}

sealed class CreateFavUiEvent {
  data class TapDigit(val digit: String) : CreateFavUiEvent()

  data object TapNext : CreateFavUiEvent()

  data object TapDelete : CreateFavUiEvent()

  data class InsertFavorite(val sorteo: List<String>) : CreateFavUiEvent()

  data object NavigateBack : CreateFavUiEvent()

  data class ClearEvent(val clearable: Clearable) : CreateFavUiEvent()

  data object ShowMessage : CreateFavUiEvent()
}

class CreateFavoriteScreenViewModel(private val useCases: FavoritesUseCases) : ScreenModel {
  companion object {
    const val MIN_LEN = 0
    const val MAX_LEN = 6
    private const val ERROR_EMPTY_INPUT = "Ingresa un numero."
    private const val ERROR_ONLY_DIGITS = "Solo se permite ingresar numeros."
    private const val ERROR_INPUT_ABOVE_56 = "Solo se permiten numeros hasta 56."
    private const val ERROR_ALREADY_ADDED = "Numero agregado previamente."
    private const val MSG_COMPLETED_DRAW = "El sorteo esta completo, presiona \u2713 para guardarlo"
  }

  private val _state = MutableStateFlow(CreateFavoriteScreenState())
  val state =
      _state
          .asStateFlow()
          .stateIn(screenModelScope, SharingStarted.Eagerly, CreateFavoriteScreenState())

  fun sendEvent(event: CreateFavUiEvent) {
    when (event) {
      CreateFavUiEvent.TapNext -> {
        moveToNext()
      }
      CreateFavUiEvent.TapDelete -> {
        deleteDigit()
      }
      is CreateFavUiEvent.TapDigit -> {
        captureDigit(event.digit)
      }

      is CreateFavUiEvent.InsertFavorite -> {
        insertFavorite(event.sorteo)
      }

      CreateFavUiEvent.NavigateBack -> {
        launchBackNavigation()
      }

      is CreateFavUiEvent.ClearEvent -> clear(event.clearable)
      is CreateFavUiEvent.ShowMessage -> showMessage(true)
    }
  }

  private fun clear(clearable: Clearable) {
    when (clearable) {
      Clearable.BACK_NAVIGATION -> clearBackNavigation()
      Clearable.SORTEO_COMPLETED -> clearSorteoCompleted()
      Clearable.CAPTURE_NUMBER -> clearCaptureNumber()
      Clearable.ERROR -> clearError()
      Clearable.AFTER_STORAGE -> clearAfterStorage()
      Clearable.MESSAGE -> showMessage(false)
    }
  }

  private fun deleteDigit() {
    val currentInput = state.value.keyboardInput
    if (currentInput.isEmpty()) {
      val currentNumbers = state.value.numbers
      if (currentNumbers.isNotEmpty()) {
        _state.update { state -> state.copy(numbers = currentNumbers.dropLast(1)) }
      } else {
        _state.update { state -> state.copy(navigateBack = true) }
      }
    } else {
      if (currentInput.length == 1) {
        _state.update { state -> state.copy(keyboardInput = "") }
      } else {
        _state.update { state -> state.copy(keyboardInput = currentInput.dropLast(1)) }
      }
    }
  }

  private fun clearBackNavigation() {
    _state.update { state -> state.copy(navigateBack = false) }
  }

  private fun clearSorteoCompleted() {
    _state.update { state -> state.copy(sorteoCompleted = emptyList()) }
  }

  private fun clearCaptureNumber() {
    _state.update { state -> state.copy(keyboardInput = "") }
  }

  private fun clearError() {
    _state.update { state -> state.copy(captureError = "") }
  }

  private fun clearAfterStorage() {
    _state.update { state -> state.clearFlags().copy(sorteoStored = true) }
  }

  private fun moveToNext() {
    val currentInput = state.value.keyboardInput
    val currentNumbers = state.value.numbers
    if (currentNumbers.size == MAX_LEN) {
      // show storage prompt
      _state.update { state -> state.copy(sorteoCompleted = currentNumbers) }
    } else {
      when {
        currentInput.isBlank() ->
            _state.update { state -> state.copy(captureError = ERROR_EMPTY_INPUT) }
        !currentInput.isDigitsOnly() ->
            _state.update { state -> state.copy(captureError = ERROR_ONLY_DIGITS) }
        currentInput.toInt() > 56 ->
            _state.update { state -> state.copy(captureError = ERROR_INPUT_ABOVE_56) }
        isNumberInSorteo(currentInput) ->
            _state.update { state -> state.copy(captureError = ERROR_ALREADY_ADDED) }
        else -> addNumberToSorteo(currentInput)
      }
    }
  }

  private fun captureDigit(digit: String) {
    Logger.d { "Capturing digit: $digit" }
    val numbersSize = state.value.numbers.size
    var currentInput = state.value.keyboardInput
    if (currentInput.length < 2) {
      if (numbersSize < MAX_LEN) {
        currentInput += digit
      } else {
        _state.update { state -> state.copy(captureError = MSG_COMPLETED_DRAW) }
        currentInput = ""
      }
      _state.update { state -> state.copy(keyboardInput = currentInput) }
    }
  }

  @OptIn(ExperimentalTime::class)
  private fun insertFavorite(sorteo: List<String>) {
    screenModelScope.launch {
      val map = sorteo.map { it.toInt() }.sorted().map { it.toString() }
      val model =
          FavoritoModel(
              0,
              map.prettyPrint(),
              FavOrigin.Manual,
              Clock.System.now().toEpochMilliseconds(),
              // ZonedDateTime.now().toInstant().toEpochMilli(),
          )
      useCases.addFavorite(model).also {
        _state.update { state -> state.copy(sorteoInserted = true) }
      }
    }
  }

  private fun showMessage(show: Boolean) {
    _state.update { state -> state.copy(sorteoStored = show) }
  }

  private fun addNumberToSorteo(number: String) {
    Logger.d { "Adding number to sorteo: $number" }
    val currentNumbers = state.value.numbers.toMutableList()
    if (currentNumbers.size in MIN_LEN until MAX_LEN) {
      Logger.d { "Sorteo not complete, adding $number, to index: ${currentNumbers.size}" }
      currentNumbers.add(number)
      _state.update { state -> state.copy(numbers = currentNumbers, keyboardInput = "") }
    }
  }

  private fun isNumberInSorteo(number: String): Boolean {
    return state.value.numbers.contains(number)
  }

  private fun launchBackNavigation() {
    _state.update { state -> state.copy(navigateBack = true) }
  }
}
