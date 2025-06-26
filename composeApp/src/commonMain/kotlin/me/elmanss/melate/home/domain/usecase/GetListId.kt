package me.elmanss.melate.home.domain.usecase

import java.util.Random

class GetListId(private val random: Random) {
  operator fun invoke() = random.nextInt()
}
