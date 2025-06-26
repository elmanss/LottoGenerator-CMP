package me.elmanss.melate.common.data.repository

import app.cash.sqldelight.coroutines.asFlow
import me.elmanss.melate.common.data.local.FavOrigin
import me.elmanss.melate.data.FavoritoQueries

class FavoritosRepositoryImpl(private val dao: FavoritoQueries) : FavoritosRepository {
  override suspend fun createFavoritos(sorteoString: String, origin: FavOrigin, createdAt: Long) {
    dao.insertFav(sorteoString, origin.name, createdAt)
  }

  override fun selectAllFavoritos() = dao.selectAll().asFlow()

  override suspend fun deleteFavorito(favoritoId: Long) {
    dao.deleteFav(favoritoId)
  }
}
