package me.elmanss.melate.home.presentation.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
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
  ConstraintLayout(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(16.dp)
              .combinedClickable(
                  onClick = { onClick.invoke(sorteo) },
                  onLongClick = {
                    actionState = true
                    sorteo.selected = true
                    onLongClick.invoke(sorteo)
                  },
              )) {
        val (text, check) = createRefs()

        if (selectableMode) {
          Checkbox(
              modifier =
                  modifier.wrapContentWidth().constrainAs(check) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.wrapContent
                  },
              checked = actionState,
              onCheckedChange = {
                actionState = !actionState
                sorteo.selected = actionState
                onChecked.invoke(sorteo)
              },
          )
        } else {
          actionState = false
        }

        Text(
            text = sorteo.numeros.joinToString(),
            textAlign = TextAlign.Start,
            modifier =
                modifier.constrainAs(text) {
                  start.linkTo(parent.start)
                  top.linkTo(parent.top)
                  bottom.linkTo(parent.bottom)
                  width = Dimension.fillToConstraints
                  height = Dimension.wrapContent
                },
        )
      }
}
