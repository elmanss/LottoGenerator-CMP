package me.elmanss.melate.home.data.remote

interface SorteoApi {
  fun fetchSorteos(): List<Int>
}
