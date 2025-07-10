package me.elmanss.melate.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

// Actual implementation for Android
actual class NetworkConnectivityObserver(
    private val context: Context
) { // Made constructor internal if factory is used

  private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  @SuppressLint("MissingPermission") // Ensure you handle permissions correctly where this is used
  actual fun observe(): Flow<NetworkStatus> =
      callbackFlow {
            val networkCallback =
                object : ConnectivityManager.NetworkCallback() {
                  override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(NetworkStatus.Available) }
                  }

                  override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(NetworkStatus.Unavailable) }
                  }

                  // This callback can be called multiple times in specific scenarios.
                  // onCapabilitiesChanged might be more robust for determining actual internet.
                  override fun onCapabilitiesChanged(
                      network: Network,
                      networkCapabilities: NetworkCapabilities
                  ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    if (networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                      // NET_CAPABILITY_VALIDATED means the system confirmed internet access
                      launch { send(NetworkStatus.Available) }
                    } else {
                      // Consider if simply losing a capability means "Unavailable" or if onLost is
                      // sufficient
                      // For simplicity here, let's keep it tied to onLost/onAvailable for the
                      // primary signal
                    }
                  }

                  override fun onUnavailable() { // For older APIs when no network is available
                    // initially
                    super.onUnavailable()
                    launch { send(NetworkStatus.Unavailable) }
                  }
                }

            // Initial check (important)
            val currentNetwork = connectivityManager.activeNetwork
            if (currentNetwork == null) {
              launch { send(NetworkStatus.Unavailable) }
            } else {
              val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
              if (capabilities == null ||
                  (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                      !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
                      !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) &&
                      !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) || // Added VPN
                  !capabilities.hasCapability(
                      NetworkCapabilities.NET_CAPABILITY_INTERNET) || // Check for internet
                  !capabilities.hasCapability(
                      NetworkCapabilities.NET_CAPABILITY_VALIDATED) // Check if validated
              ) {
                launch { send(NetworkStatus.Unavailable) }
              } else {
                launch { send(NetworkStatus.Available) }
              }
            }

            val networkRequest =
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    // Listen to default network changes, which is generally what you want for
                    // "internet access"
                    // .addTransportType(NetworkCapabilities.TRANSPORT_WIFI) // You can be more
                    // specific if needed
                    // .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    // .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                    // .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
                    .build()

            // You need ACCESS_NETWORK_STATE permission for this.
            try {
              connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            } catch (e: SecurityException) {
              // Handle cases where permission might be missing, though it should be declared
              // For a flow, you might want to send an error state or a specific NetworkStatus
              launch { send(NetworkStatus.Unavailable) } // Or a new "PermissionError" status
            }

            awaitClose { connectivityManager.unregisterNetworkCallback(networkCallback) }
          }
          .distinctUntilChanged()
}

// Actual factory for Android (requires Context)
// This function needs to be defined in a file that can access Android 'Context'
// Typically, you'd get the Context from your Application class or an Activity/Fragment
// and pass it when creating the observer.
// For KMP, you might inject this Context via a platform-specific injector or service locator.

// A common way to get context in KMP for Android is by having an application context holder.
// For simplicity, let's assume you'll pass it when you create an instance of the class that uses
// this.
// So, the `createNetworkConnectivityObserver` might look like this IF you have a way to get Context
// from commonMain (which you usually don't directly).

// It's often better to make the 'actual class' constructor require 'Context' and then
// your platform-specific Android code instantiates it.
// So, the common `createNetworkConnectivityObserver` might not take parameters.
// The `actual constructor` on Android WILL take `Context`.

// Let's refine the factory approach slightly:
// In your Android app module (app/build.gradle.kts), you'd provide the context when needed.
// The `createNetworkConnectivityObserver` is a bridge.
// A more KMP-idiomatic way to handle context is to have it injected into the actual class.
// The `expect fun createNetworkConnectivityObserver` would then be parameterless.

// --- If your commonMain `createNetworkConnectivityObserver` is parameterless ---
// This assumes you get context from somewhere (e.g., a static holder - not always ideal, or DI)
// For providing context, it's better to pass it to the constructor of the `actual class`
// and have your `androidApp` module or DI framework handle the instantiation.

// Let's assume you get context where you need the observer.
// Then in your Android code (e.g., ViewModel init):
// val observer = NetworkConnectivityObserver(applicationContext)
// val statusFlow = observer.observe()

// So, the `actual fun createNetworkConnectivityObserver` becomes simpler IF you
// decide to instantiate `NetworkConnectivityObserver` directly in Android code where context is
// available.
// If you want a common factory, you need a way to pass context.
// For now, let's assume direct instantiation in platform code is fine,
// and the `expect class` is the main contract.

// To use the factory pattern like in your initial thought:
// `androidMain/kotlin/your/package/PlatformSpecific.android.kt`
// object AndroidPlatformDependencies { // Or however you manage your platform-specific context
//  lateinit var appContext: Context
// }