package me.elmanss.melate.home.domain.usecase

import co.touchlab.kermit.Logger
import me.elmanss.melate.common.data.local.FavOrigin
import me.elmanss.melate.common.data.repository.FavoritosRepository
import me.elmanss.melate.home.domain.model.SorteoModel

class SaveToFavorites(private val repository: FavoritosRepository) {
  suspend operator fun invoke(sorteo: SorteoModel, createdAt: Long) {
    val storable = sorteo.prettyPrint()
    Logger.d { "Saving $storable to database" }
    repository.createFavoritos(storable, FavOrigin.Random, createdAt)
  }
}
