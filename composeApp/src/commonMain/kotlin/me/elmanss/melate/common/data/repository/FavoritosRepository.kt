package me.elmanss.melate.common.data.repository

import app.cash.sqldelight.Query
import kotlinx.coroutines.flow.Flow
import me.elmanss.melate.common.data.local.FavOrigin
import me.elmanss.melate.data.Favorito

interface FavoritosRepository {

  suspend fun createFavoritos(
    sorteoString: String,
    origin: FavOrigin = FavOrigin.Random,
    createdAt: Long = 0L,
  )

  fun selectAllFavoritos(): Flow<Query<Favorito>>

  suspend fun deleteFavorito(favoritoId: Long)
}
