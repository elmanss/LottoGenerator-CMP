package me.elmanss.melate.common.di

import app.cash.sqldelight.db.SqlDriver
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import me.elmanss.melate.Database
import me.elmanss.melate.SorteoApiImpl
import me.elmanss.melate.common.data.local.SorteoLocalDataSource
import me.elmanss.melate.common.data.network.SorteoRemoteDataSource
import me.elmanss.melate.common.data.repository.FavoritosRepository
import me.elmanss.melate.common.data.repository.FavoritosRepositoryImpl
import me.elmanss.melate.common.domain.SorteoApi
import me.elmanss.melate.common.domain.datasource.SorteoDataSource
import me.elmanss.melate.common.util.NetworkConnectivityObserver
import me.elmanss.melate.create.presentation.CreateFavoriteScreenViewModel
import me.elmanss.melate.data.FavoritoQueries
import me.elmanss.melate.favorites.domain.usecase.AddFavorite
import me.elmanss.melate.favorites.domain.usecase.DeleteFavorite
import me.elmanss.melate.favorites.domain.usecase.FavoritesUseCases
import me.elmanss.melate.favorites.domain.usecase.FetchFavorites
import me.elmanss.melate.favorites.domain.usecase.FetchSorteoFromNetwork
import me.elmanss.melate.favorites.domain.usecase.FormatFavCreationDate
import me.elmanss.melate.favorites.presentation.ListFavoritesScreenViewModel
import me.elmanss.melate.home.data.repository.SorteoRepositoryImpl
import me.elmanss.melate.home.domain.repository.SorteoRepository
import me.elmanss.melate.home.domain.usecase.FetchSorteos
import me.elmanss.melate.home.domain.usecase.GetListId
import me.elmanss.melate.home.domain.usecase.HomeUseCases
import me.elmanss.melate.home.domain.usecase.SaveToFavorites
import me.elmanss.melate.home.presentation.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

fun homeModule() = module {
  factory<Random> { ThreadLocalRandom.current() }

  factory<SorteoDataSource>(named("localDS")) { SorteoLocalDataSource(get(), 1..56) }

  factory<SorteoRepository>(named("localRepo")) { SorteoRepositoryImpl(get(named("localDS"))) }

  factory {
    HomeUseCases(FetchSorteos(get(named("localRepo"))), SaveToFavorites(get()), GetListId(get()))
  }

  factory { HomeViewModel(get()) }
}

@OptIn(FormatStringsInDatetimeFormats::class)
fun favoritesModule() = module {
  factory {
    val formatPattern = "dd/MM/yyyy HH:mm:ss"
    LocalDateTime.Format { byUnicodePattern(formatPattern) }
  }

  factory<SorteoDataSource>(named("remoteDS")) { SorteoRemoteDataSource(get()) }

  factory<SorteoRepository>(named("remoteRepo")) { SorteoRepositoryImpl(get(named("remoteDS"))) }

  factory {
    FavoritesUseCases(
        addFavorite = AddFavorite(get()),
        deleteFavorite = DeleteFavorite(get()),
        fetchFavorites = FetchFavorites(get()),
        formatFavoriteCreationDate = FormatFavCreationDate(get()),
        fetchSorteoFromNetwork = FetchSorteoFromNetwork(get(named("remoteRepo"))))
  }

  factory { ListFavoritesScreenViewModel(get(), get()) }

  factory { CreateFavoriteScreenViewModel(get()) }
}

fun appModule(driver: SqlDriver, connectivityObserver: NetworkConnectivityObserver) = module {
  includes(homeModule(), favoritesModule())
  single {
    Database.Schema.create(driver)
    Database(driver)
  }

  single<NetworkConnectivityObserver> { connectivityObserver }
  single<FavoritoQueries> {
    val database = get<Database>()
    database.favoritoQueries
  }

  single<SorteoApi> { SorteoApiImpl() }

  single<FavoritosRepository> { FavoritosRepositoryImpl(get()) }
}

fun initializeKoin(driver: SqlDriver, connectivityObserver: NetworkConnectivityObserver) {
  if (KoinPlatform.getKoinOrNull() == null) {
    startKoin { modules(appModule(driver, connectivityObserver)) }
  }
}
