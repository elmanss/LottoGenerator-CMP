package me.elmanss.melate.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.Dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lottogeneratorcmp.composeapp.generated.resources.Res
import lottogeneratorcmp.composeapp.generated.resources.app_name
import lottogeneratorcmp.composeapp.generated.resources.txt_action_add
import lottogeneratorcmp.composeapp.generated.resources.txt_button_mis_favs
import lottogeneratorcmp.composeapp.generated.resources.txt_msg_add_to_fav
import lottogeneratorcmp.composeapp.generated.resources.txt_sorteo_success
import lottogeneratorcmp.composeapp.generated.resources.txt_title_aviso
import me.elmanss.melate.common.presentation.component.MelateActionTopBar
import me.elmanss.melate.common.presentation.component.MelateFab
import me.elmanss.melate.common.presentation.component.MelateSorteoActionDialog
import me.elmanss.melate.favorites.presentation.ListFavoritesScreen
import me.elmanss.melate.getPlatform
import me.elmanss.melate.home.presentation.components.HomeListItem
import org.jetbrains.compose.resources.stringResource

class HomeScreen : Screen {

  @OptIn(
      ExperimentalMaterial3Api::class,
      ExperimentalStdlibApi::class,
      ExperimentalComposeUiApi::class)
  @Composable
  override fun Content() {

    val viewModel = getScreenModel<HomeViewModel>()

    val uiState = viewModel.state.collectAsState()
    val sorteoState = rememberLazyListState()
    val refreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val snackbarState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var multiselectState by rememberSaveable { mutableStateOf(false) }
    val localNavigator = LocalNavigator.current

    BackHandler(enabled = multiselectState) {
      Logger.d { "Back pressed" }
      viewModel.sendEvent(HomeUiEvent.ExitMultiSelect)
    }

    Scaffold(
        topBar = {
          MelateActionTopBar(
              title = stringResource(Res.string.app_name),
          ) {
            if (multiselectState) {
              IconButton(onClick = { viewModel.sendEvent(HomeUiEvent.ConfirmMultiSelect) }) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
              }
            } else {
              if (getPlatform().name != "Android") {
                IconButton(onClick = { viewModel.sendEvent(HomeUiEvent.RefreshSorteos) }) {
                  Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                }
              }
            }
          }
        },
        floatingActionButton = {
          MelateFab(
              listState = sorteoState,
              action = { viewModel.sendEvent(HomeUiEvent.GoToFavs) },
              text = stringResource(Res.string.txt_button_mis_favs),
          )
        },
        snackbarHost = { SnackbarHost(snackbarState) },
    ) {
      multiselectState = uiState.value.multiSelectMode

      Column(modifier = Modifier.fillMaxWidth().padding(it)) {
        val sorteos = uiState.value.sorteos
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
              isRefreshing = true
              coroutineScope.launch {
                delay(1500)
                viewModel.sendEvent(HomeUiEvent.RefreshSorteos)
                isRefreshing = false
              }
            },
            state = refreshState,
            indicator = {
              Indicator(
                  modifier = Modifier.align(Alignment.TopCenter),
                  isRefreshing = isRefreshing,
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  state = refreshState,
              )
            },
        ) {
          LazyColumn(state = sorteoState) {
            itemsIndexed(items = sorteos, key = { index, _ -> index.toHexString() }) { index, sorteo
              ->
              HomeListItem(
                  selectableMode = multiselectState,
                  sorteo = sorteo,
                  onChecked = { s -> HomeUiEvent.SelectSorteo(s, index) },
                  onClick = { s ->
                    if (!multiselectState) {
                      viewModel.sendEvent(HomeUiEvent.ShowSaveSorteoDialog(s))
                    }
                  },
              ) { s ->
                if (!multiselectState) {
                  viewModel.sendEvent(HomeUiEvent.EnableSorteoMultiSelect(s, index))
                }
              }

              if (index < sorteos.lastIndex) {
                HorizontalDivider(thickness = Dp.Hairline)
              }
            }
          }
        }

        uiState.value.clickedSorteo?.let { sorteo ->
          MelateSorteoActionDialog(
              { viewModel.sendEvent(HomeUiEvent.HideSaveSorteoDialog) },
              { viewModel.sendEvent(HomeUiEvent.ConfirmSaveSorteo(sorteo)) },
              title = stringResource(Res.string.txt_title_aviso),
              msg = stringResource(Res.string.txt_msg_add_to_fav),
              actionTxt = stringResource(Res.string.txt_action_add),
          )
        }

        if (uiState.value.showStorageSuccess) {
          val successMsg = stringResource(Res.string.txt_sorteo_success)
          LaunchedEffect(true) {
            val result =
                snackbarState.showSnackbar(message = successMsg, duration = SnackbarDuration.Short)
            when (result) {
              SnackbarResult.Dismissed -> {
                viewModel.sendEvent(HomeUiEvent.DisplaySuccessMessage(visible = true))
              }

              SnackbarResult.ActionPerformed -> {}
            }
          }
        }
      }
    }

    if (uiState.value.onGoToFav) {
      localNavigator?.let {
        it.push(ListFavoritesScreen())
        viewModel.sendEvent(HomeUiEvent.ClearFlags)
      }
    }
  }
}
