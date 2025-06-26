package me.elmanss.melate.favorites.domain.usecase

import me.elmanss.melate.common.data.repository.FavoritosRepository
import me.elmanss.melate.favorites.domain.model.FavoritoModel

class AddFavorite(private val repository: FavoritosRepository) {
  suspend operator fun invoke(model: FavoritoModel) {
    repository.createFavoritos(model.sorteo, model.origin, model.createdAt)
  }
}
