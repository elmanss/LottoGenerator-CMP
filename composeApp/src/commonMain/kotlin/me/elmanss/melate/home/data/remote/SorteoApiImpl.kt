package me.elmanss.melate.home.data.remote

import co.touchlab.kermit.Logger
import me.elmanss.melate.common.util.takeRandom
import java.util.Random

class SorteoApiImpl(private val random: Random, private val sorteoRange: IntRange) : SorteoApi {
  override fun fetchSorteos(): List<Int> {
    val mutableRandomDraw = mutableSetOf<Int>()
    val shuffledElements = sorteoRange.shuffled(random).toMutableList()
    Logger.d { "draw starting" }
    fillSet(shuffledElements, mutableRandomDraw, random.nextLong())
    Logger.d { "draw completed" }
    return mutableRandomDraw.sorted()
  }

  private fun fillSet(origin: MutableList<Int>, destinationSet: MutableSet<Int>, seed: Long) {
    while (destinationSet.size < 6) {
      if (origin.isNotEmpty()) {
        val randomElement = origin.takeRandom(kotlin.random.Random(seed))
        destinationSet.add(randomElement)
        Logger.d { "element $randomElement added successfully" }
      } else {
        Logger.w { "draw source is empty" }
        break
      }
    }
  }
}
