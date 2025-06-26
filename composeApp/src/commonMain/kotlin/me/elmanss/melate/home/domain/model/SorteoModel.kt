package me.elmanss.melate.home.domain.model

data class SorteoModel(val numeros: List<Int>, var selected: Boolean = false) {
  fun prettyPrint(): String {
    return this.numeros.joinToString(separator = ", ", transform = { it.toString() })
  }
}
