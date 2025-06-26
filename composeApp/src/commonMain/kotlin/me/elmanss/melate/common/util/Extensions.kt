package me.elmanss.melate.common.util

import kotlin.random.Random

fun List<String>?.prettyPrint(): String {
  return this?.joinToString(", ") ?: ""
}

@Throws(NoSuchElementException::class)
fun <T> MutableList<T>.takeRandom(random: Random = Random): T {
  if (isEmpty()) {
    throw NoSuchElementException("List is empty")
  }

  val randomIndex = random.nextInt(size)
  return removeAt(randomIndex)
}

fun <T> MutableList<T>.legacyRemoveLast(): T = this.removeAt(this.lastIndex)

fun Throwable.getRootCause(): Throwable {
  var rootCause: Throwable = this
  while (rootCause.cause != null) {
    rootCause = rootCause.cause!!
  }
  return rootCause
}

fun Throwable.getRootCauseWithCycleGuard(): Throwable {
  var rootCause: Throwable = this
  val visited = mutableSetOf<Throwable>()
  while (rootCause.cause != null && rootCause.cause != rootCause && !visited.contains(rootCause)) {
    visited.add(rootCause)
    rootCause = rootCause.cause!!
    println("Current root cause: $rootCause")
  }

  println("Visited: $visited")
  println("Final root cause: $rootCause")
  return rootCause
}

fun CharSequence.isDigitsOnly(): Boolean = this.all { it.isDigit() }
