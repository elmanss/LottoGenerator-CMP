package me.elmanss.melate.common.di

import app.cash.sqldelight.db.SqlDriver
import java.util.Random
import java.util.concurrent.ThreadLocalRandom
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import me.elmanss.melate.Database
import me.elmanss.melate.common.data.repository.FavoritosRepository
import me.elmanss.melate.common.data.repository.FavoritosRepositoryImpl
import me.elmanss.melate.create.presentation.CreateFavoriteScreenViewModel
import me.elmanss.melate.data.FavoritoQueries
import me.elmanss.melate.favorites.domain.usecase.AddFavorite
import me.elmanss.melate.favorites.domain.usecase.DeleteFavorite
import me.elmanss.melate.favorites.domain.usecase.FavoritesUseCases
import me.elmanss.melate.favorites.domain.usecase.FetchFavorites
import me.elmanss.melate.favorites.domain.usecase.FormatFavCreationDate
import me.elmanss.melate.favorites.presentation.ListFavoritesScreenViewModel
import me.elmanss.melate.home.data.remote.SorteoApi
import me.elmanss.melate.home.data.remote.SorteoApiImpl
import me.elmanss.melate.home.data.repository.SorteoRepositoryImpl
import me.elmanss.melate.home.domain.repository.SorteoRepository
import me.elmanss.melate.home.domain.usecase.FetchSorteos
import me.elmanss.melate.home.domain.usecase.GetListId
import me.elmanss.melate.home.domain.usecase.HomeUseCases
import me.elmanss.melate.home.domain.usecase.SaveToFavorites
import me.elmanss.melate.home.presentation.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

@OptIn(FormatStringsInDatetimeFormats::class)
fun appModule(driver: SqlDriver) = module {
  single {
    Database.Schema.create(driver)
    Database(driver)
  }
  single<FavoritoQueries> {
    val database = get<Database>()
    database.favoritoQueries
  }
  single<FavoritosRepository> { FavoritosRepositoryImpl(get()) }

  factory<Random> { ThreadLocalRandom.current() }

  factory<SorteoApi> { SorteoApiImpl(get(), 1..56) }

  factory<SorteoRepository> { SorteoRepositoryImpl(get()) }

  factory { HomeUseCases(FetchSorteos(get()), SaveToFavorites(get()), GetListId(get())) }

  factory { HomeViewModel(get()) }

  factory {
    val formatPattern = "dd/MM/yyyy HH:mm:ss"
    LocalDateTime.Format { byUnicodePattern(formatPattern) }
  }

  factory {
    FavoritesUseCases(
        AddFavorite(get()),
        DeleteFavorite(get()),
        FetchFavorites(get()),
        FormatFavCreationDate(get()))
  }

  factory { ListFavoritesScreenViewModel(get()) }

  factory { CreateFavoriteScreenViewModel(get()) }
}

fun initializeKoin(driver: SqlDriver) {
  startKoin { modules(appModule(driver)) }
}
