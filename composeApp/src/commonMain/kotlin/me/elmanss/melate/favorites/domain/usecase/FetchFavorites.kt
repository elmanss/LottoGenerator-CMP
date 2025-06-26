package me.elmanss.melate.favorites.domain.usecase

import kotlinx.coroutines.flow.map
import me.elmanss.melate.common.data.local.FavOrigin
import me.elmanss.melate.common.data.repository.FavoritosRepository
import me.elmanss.melate.favorites.domain.model.FavoritoModel

class FetchFavorites(private val repository: FavoritosRepository) {
  operator fun invoke() =
      repository
          .selectAllFavoritos()
          .map { it.executeAsList() }
          .map {
            it.map { FavoritoModel(it.id, it.sorteo, FavOrigin.valueOf(it.origin), it.created_at) }
          }
}
