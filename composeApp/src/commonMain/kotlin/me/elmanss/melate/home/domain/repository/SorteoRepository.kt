package me.elmanss.melate.home.domain.repository

interface SorteoRepository {

  fun fetchSorteos(): List<Int>
}
