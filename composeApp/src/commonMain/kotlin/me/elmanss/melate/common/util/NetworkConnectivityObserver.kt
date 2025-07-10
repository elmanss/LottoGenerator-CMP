package me.elmanss.melate.common.util

import kotlinx.coroutines.flow.Flow

// Common Enum accessible from all modules
enum class NetworkStatus {
  Available,
  Unavailable
}

// Expected interface/class for the observer
expect class NetworkConnectivityObserver {
  fun observe(): Flow<NetworkStatus>
}