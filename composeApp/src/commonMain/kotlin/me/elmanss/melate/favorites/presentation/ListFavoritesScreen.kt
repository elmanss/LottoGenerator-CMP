package me.elmanss.melate.favorites.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import co.touchlab.kermit.Logger
import lottogeneratorcmp.composeapp.generated.resources.Res
import lottogeneratorcmp.composeapp.generated.resources.cloud
import lottogeneratorcmp.composeapp.generated.resources.heart_plus_24px
import lottogeneratorcmp.composeapp.generated.resources.human_edit
import lottogeneratorcmp.composeapp.generated.resources.txt_action_delete
import lottogeneratorcmp.composeapp.generated.resources.txt_empty_fav_msg
import lottogeneratorcmp.composeapp.generated.resources.txt_fav_deletion_success
import lottogeneratorcmp.composeapp.generated.resources.txt_mis_sorteos
import lottogeneratorcmp.composeapp.generated.resources.txt_msg_delete_fav
import lottogeneratorcmp.composeapp.generated.resources.txt_title_aviso
import me.elmanss.melate.common.presentation.component.MelateActionExtendedFab
import me.elmanss.melate.common.presentation.component.MelatePlatformDependentActionTopBar
import me.elmanss.melate.common.presentation.component.MelateSorteoActionDialog
import me.elmanss.melate.common.presentation.theme.Gray
import me.elmanss.melate.common.presentation.theme.infoTextSize
import me.elmanss.melate.common.presentation.theme.keySize
import me.elmanss.melate.common.util.NetworkStatus
import me.elmanss.melate.create.presentation.CreateFavoriteScreen
import me.elmanss.melate.favorites.presentation.components.ListFavoriteItem
import me.elmanss.melate.getPlatform
import org.jetbrains.compose.resources.stringResource

class ListFavoritesScreen : Screen {
  @OptIn(ExperimentalComposeUiApi::class)
  @Composable
  override fun Content() {
    val viewModel = getScreenModel<ListFavoritesScreenViewModel>()
    val uiState = viewModel.state.collectAsState()
    val connectivityState by viewModel.connectivity.collectAsState(NetworkStatus.Unavailable)
    val sorteoState = rememberLazyListState()
    val snackbarState = remember { SnackbarHostState() }
    var multiselectState by remember { mutableStateOf(false) }
    val localNavigator = LocalNavigator.current

    BackHandler(enabled = multiselectState) {
      Logger.d { "Back pressed" }
      viewModel.sendEvent(ListFavUiEvent.DisableMultiDelete)
    }

    Scaffold(
        topBar = {
          Column {
            MelatePlatformDependentActionTopBar(
                platform = getPlatform(),
                title = stringResource(Res.string.txt_mis_sorteos),
                onBack = { viewModel.sendEvent(ListFavUiEvent.NavigateBack) }) {
                  if (multiselectState) {
                    IconButton(
                        onClick = {
                          viewModel.sendEvent(ListFavUiEvent.ShowMultiDeleteFavDialog)
                        }) {
                          Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                  }
                }

            if (!multiselectState && uiState.value.isLoading)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
          }
        },
        floatingActionButton = {
          if (!multiselectState) {
            MelateActionExtendedFab(
                listState = sorteoState,
                actionOneIcon = org.jetbrains.compose.resources.vectorResource(Res.drawable.cloud),
                onActionOneClicked = {
                  if (connectivityState == NetworkStatus.Available) {
                    Logger.d { "Has internet connection" }
                    viewModel.sendEvent(ListFavUiEvent.ShowLoader)
                    viewModel.sendEvent(ListFavUiEvent.FetchFavFromNetwork)
                  } else {
                    Logger.e { "No internet connection" }
                    viewModel.sendEvent(ListFavUiEvent.ShowConnectivityMessage(true))
                  }
                },
                actionTwoIcon =
                    org.jetbrains.compose.resources.vectorResource(Res.drawable.human_edit),
            ) {
              viewModel.sendEvent(ListFavUiEvent.GoToCreate)
            }
          }
        },
        snackbarHost = { SnackbarHost(snackbarState) },
    ) {
      multiselectState = uiState.value.multiselectEnabled
      val favs = uiState.value.favs
      if (favs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16F.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
          Image(
              painter =
                  org.jetbrains.compose.resources.painterResource(Res.drawable.heart_plus_24px),
              modifier = Modifier.size(keySize).align(Alignment.CenterHorizontally),
              contentDescription = "Add fav icon",
          )
          Text(
              color = Gray,
              fontSize = infoTextSize,
              text = stringResource(Res.string.txt_empty_fav_msg),
              textAlign = TextAlign.Center,
          )
        }
      } else {
        Column(modifier = Modifier.fillMaxSize().padding(it)) {
          LazyColumn(state = sorteoState) {
            itemsIndexed(favs) { index, fav ->
              ListFavoriteItem(
                  editableState = multiselectState,
                  favorite = fav,
                  formatter = { viewModel.formatDate(fav) },
                  onChecked = { f -> viewModel.sendEvent(ListFavUiEvent.SelectFav(fav, index)) },
                  onLongClick = { f ->
                    if (!multiselectState) {
                      viewModel.sendEvent(ListFavUiEvent.EnableMultiDelete(fav, index))
                    }
                  },
              ) {
                if (!multiselectState) {
                  viewModel.sendEvent(ListFavUiEvent.ShowDeleteFavDialog(fav))
                }
              }

              if (index < favs.lastIndex) {
                HorizontalDivider(thickness = Dp.Hairline)
              }
            }
          }
        }
      }

      uiState.value.favToDelete?.let { sorteo ->
        MelateSorteoActionDialog(
            { viewModel.sendEvent(ListFavUiEvent.HideDeleteFavDialog) },
            { viewModel.sendEvent(ListFavUiEvent.DeleteFav(sorteo)) },
            stringResource(Res.string.txt_title_aviso),
            stringResource(Res.string.txt_msg_delete_fav),
            stringResource(Res.string.txt_action_delete),
        )
      }

      if (uiState.value.showDeletionSuccess.first) {
        val successMsg =
            uiState.value.showDeletionSuccess.second.ifEmpty {
              stringResource(Res.string.txt_fav_deletion_success)
            }
        LaunchedEffect(true) {
          val result =
              snackbarState.showSnackbar(message = successMsg, duration = SnackbarDuration.Short)
          when (result) {
            SnackbarResult.Dismissed -> {
              viewModel.sendEvent(ListFavUiEvent.HideSuccessMessage)
            }

            SnackbarResult.ActionPerformed -> {}
          }
        }
      }
    }

    if (uiState.value.showMultiDeletionPrompt) {
      MelateSorteoActionDialog(
          { viewModel.sendEvent(ListFavUiEvent.HideMultiDeleteFavDialog) },
          { viewModel.sendEvent(ListFavUiEvent.DeleteMultipleFavs) },
          stringResource(Res.string.txt_title_aviso),
          "Se eliminaran los sorteos seleccionados.",
          stringResource(Res.string.txt_action_delete),
      )
    }

    if (uiState.value.multideleteCompleted) {
      viewModel.sendEvent(ListFavUiEvent.DisableMultiDelete)
      viewModel.sendEvent(ListFavUiEvent.HideMultiDeleteFavDialog)
      viewModel.sendEvent(ListFavUiEvent.ClearFlags)
    }

    if (uiState.value.favTapped) {
      localNavigator?.let {
        it.push(CreateFavoriteScreen())
        viewModel.sendEvent(ListFavUiEvent.ClearFlags)
      }
    }

    if (uiState.value.navigateBack) {
      localNavigator?.let {
        it.pop()
        viewModel.sendEvent(ListFavUiEvent.ClearFlags)
      }
    }
  }
}
