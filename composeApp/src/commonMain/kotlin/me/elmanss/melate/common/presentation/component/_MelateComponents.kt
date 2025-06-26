package me.elmanss.melate.common.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.elmanss.melate.Platform
import me.elmanss.melate.common.presentation.theme.Purple40
import me.elmanss.melate.common.presentation.theme.Purple80
import me.elmanss.melate.common.presentation.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MelateTopBar(title: String, modifier: Modifier = Modifier) {
  TopAppBar(
      title = { Text(text = title) },
      colors = getTopBarColors(isSystemInDarkTheme()),
      modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MelateActionTopBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit) = {},
) {
  TopAppBar(
      title = { Text(text = title) },
      colors = getTopBarColors(isSystemInDarkTheme()),
      modifier = modifier,
      actions = actions,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MelatePlatformDependentActionTopBar(
    platform: Platform,
    title: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
) {
  TopAppBar(
      title = { Text(text = title) },
      colors = getTopBarColors(isSystemInDarkTheme()),
      modifier = modifier,
      actions = actions,
      navigationIcon = {
        if (platform.name != "Android") {
          IconButton(onClick = { onBack.invoke() }) {
            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
          }
        }
      })
}

@OptIn(ExperimentalMaterial3Api::class)
private fun getTopBarColors(isDark: Boolean) =
    TopAppBarColors(
        containerColor = if (isDark) Purple40 else Purple80,
        scrolledContainerColor = if (isDark) Purple40 else Purple80,
        navigationIconContentColor = White,
        titleContentColor = White,
        actionIconContentColor = White,
    )

@Composable
fun MelateFab(
    action: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    listState: LazyListState,
) {
  AnimatedVisibility(visible = listState.isScrollingUp().value) {
    FloatingActionButton(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        onClick = { action.invoke() },
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
      Text(text, modifier = modifier.padding(horizontal = 4.dp))
    }
  }
}

@Composable
fun LazyListState.isScrollingUp(): State<Boolean> {
  return produceState(initialValue = true) {
    var lastIndex = 0
    var lastScroll = Int.MAX_VALUE
    snapshotFlow { firstVisibleItemIndex to firstVisibleItemScrollOffset }
        .collect { (currentIndex, currentScroll) ->
          if (currentIndex != lastIndex || currentScroll != lastScroll) {
            value =
                currentIndex < lastIndex ||
                    (currentIndex == lastIndex && currentScroll < lastScroll)
            lastIndex = currentIndex
            lastScroll = currentScroll
          }
        }
  }
}

@Composable
fun MelateSorteoActionDialog(
    onDismiss: () -> Unit,
    action: () -> Unit,
    title: String,
    msg: String,
    actionTxt: String,
    modifier: Modifier = Modifier,
) {
  Dialog({ onDismiss.invoke() }, properties = DialogProperties()) {
    Column(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(4.dp),
                )
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
          text = title,
          fontSize = TextUnit(24F, TextUnitType.Sp),
          color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(modifier.height(8.dp))
      Text(text = msg, color = MaterialTheme.colorScheme.onSurface)
      MelateDialogButton({ action.invoke() }, actionTxt)
    }
  }
}

@Composable
fun MelateDialogButton(action: () -> Unit, text: String, modifier: Modifier = Modifier) {
  val colors =
      ButtonColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          disabledContainerColor = MaterialTheme.colorScheme.inversePrimary,
      )
  TextButton(
      modifier = modifier,
      shape = RoundedCornerShape(8.dp),
      colors = colors,
      onClick = { action.invoke() },
  ) {
    Text(text)
  }
}
