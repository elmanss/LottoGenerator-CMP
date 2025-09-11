package me.elmanss.melate.home.presentation.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import me.elmanss.melate.home.domain.model.SorteoModel

@Composable
fun HomeListItem(
    modifier: Modifier = Modifier,
    selectableMode: Boolean,
    sorteo: SorteoModel,
    onChecked: (SorteoModel) -> Unit,
    onClick: (SorteoModel) -> Unit,
    onLongClick: (SorteoModel) -> Unit,
) {
  var actionState by rememberSaveable { mutableStateOf(false) }

  // Update actionState based on selectableMode when it changes
  // This handles the case where selectableMode becomes false externally
  if (!selectableMode && actionState) {
    actionState = false
    // Optionally, if sorteo.selected should also reset:
    // sorteo.selected = false
    // onChecked.invoke(sorteo) // If you need to notify about this reset
  }

  ConstraintLayout(
      modifier =
          modifier // Use the passed-in modifier for the ConstraintLayout itself
              .fillMaxWidth()
              .padding(16.dp)
              .combinedClickable(
                  onClick = {
                    // If in selectable mode and checkbox is visible,
                    // let onCheckedChange handle the state.
                    // Otherwise, perform the regular click.
                    if (selectableMode) {
                      actionState = !actionState
                      sorteo.selected = actionState
                      onChecked.invoke(sorteo)
                    } else {
                      onClick.invoke(sorteo)
                    }
                  },
                  onLongClick = {
                    if (!selectableMode) { // Only enable selectable mode via long click
                      actionState = true
                      sorteo.selected = true
                      onLongClick.invoke(
                          sorteo
                      ) // This should likely trigger selectableMode = true in parent
                    }
                  },
              )
  ) {
    val (textRef, checkRef) = createRefs()

    Text(
        text = sorteo.numeros.joinToString(),
        textAlign = TextAlign.Start,
        maxLines = 1,
        // Use a new Modifier instance for the Text
        modifier =
            Modifier.constrainAs(textRef) {
              start.linkTo(parent.start)
              top.linkTo(parent.top)
              bottom.linkTo(parent.bottom)
              height = Dimension.wrapContent
              // Conditionally set the end constraint
              if (selectableMode) {
                end.linkTo(checkRef.start, margin = 8.dp) // Link to checkbox start with a margin
              } else {
                end.linkTo(parent.end) // Link to parent end when checkbox is not visible
              }
              width = Dimension.fillToConstraints
            },
    )

    if (selectableMode) {
      Checkbox(
          // Use a new Modifier instance for the Checkbox
          modifier =
              Modifier.constrainAs(checkRef) {
                end.linkTo(parent.end)
                top.linkTo(textRef.top) // Align with text top
                bottom.linkTo(textRef.bottom) // Align with text bottom
                height = Dimension.wrapContent
                // start.linkTo(textRef.end) // Not strictly needed if Text links to checkRef.start
              },
          checked = actionState,
          onCheckedChange = { checked ->
            actionState = checked
            sorteo.selected = checked
            onChecked.invoke(sorteo)
          },
      )
    }
    // This else block for actionState = false was problematic because
    // it would run every recomposition when selectableMode is false,
    // potentially conflicting with rememberSaveable.
    // It's better handled at the top or by reacting to selectableMode changes.
    // else {
    //   actionState = false
    // }
  }
}
