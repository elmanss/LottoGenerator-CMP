package me.elmanss.melate.common.data.local

import co.touchlab.kermit.Logger
import java.util.Random
import me.elmanss.melate.common.domain.datasource.SorteoDataSource
import me.elmanss.melate.common.domain.model.RequestState
import me.elmanss.melate.common.util.takeRandom

class SorteoLocalDataSource(private val random: Random, private val sorteoRange: IntRange) :
    SorteoDataSource {
  override suspend fun fetchSorteos(): RequestState<List<Int>> {
    val mutableRandomDraw = mutableSetOf<Int>()
    val shuffledElements = sorteoRange.shuffled(random).toMutableList()
    Logger.Companion.d { "draw starting" }
    fillSet(shuffledElements, mutableRandomDraw, random.nextLong())
    Logger.Companion.d { "draw completed" }
    return RequestState.Success(mutableRandomDraw.toList())
  }

  private fun fillSet(origin: MutableList<Int>, destinationSet: MutableSet<Int>, seed: Long) {
    while (destinationSet.size < 6) {
      if (origin.isNotEmpty()) {
        val randomElement = origin.takeRandom(kotlin.random.Random(seed))
        destinationSet.add(randomElement)
        Logger.Companion.d { "element $randomElement added successfully" }
      } else {
        Logger.Companion.w { "draw source is empty" }
        break
      }
    }
  }
}
