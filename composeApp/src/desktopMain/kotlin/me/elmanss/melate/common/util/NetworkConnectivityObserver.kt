package me.elmanss.melate.common.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

actual class NetworkConnectivityObserver {

  companion object {
    private const val GOOGLE_DNS_HOST = "8.8.8.8"
    private const val GOOGLE_HTTP_HOST = "www.google.com"
    private const val CLOUDFLARE_HTTP_HOST = "www.cloudflare.com"
  }

  // This is a simplified check for JVM. For robust checking,
  // you might need to iterate NetworkInterfaces or use a library.
  // Pinging a reliable host is a common strategy.
  actual fun observe(): Flow<NetworkStatus> =
      flow {
            while (true) {
              // Check well-known HTTPS ports on reliable servers
              val googleDnsReachable = canConnectToHost(GOOGLE_DNS_HOST, 53, 1000) // DNS port
              val googleHttpReachable = canConnectToHost(GOOGLE_HTTP_HOST, 80, 1000)
              val cloudflareHttpReachable = canConnectToHost(CLOUDFLARE_HTTP_HOST, 80, 1000)

              // Or for more certainty about general internet (not just specific services being up)
              // you might prefer testing DNS resolution + a common web port.
              val currentStatus =
                  if (googleDnsReachable || googleHttpReachable || cloudflareHttpReachable) {
                    NetworkStatus.Available
                  } else {
                    NetworkStatus.Unavailable
                  }
              emit(currentStatus)
              delay(5000)
            }
          }
          .distinctUntilChanged()

  suspend fun canConnectToHost(host: String, port: Int, timeoutMillis: Int = 1500): Boolean =
      withContext(Dispatchers.IO) {
        try {
          Socket().use { socket -> // Use try-with-resources to ensure the socket is closed
            // Set a connection timeout
            socket.connect(InetSocketAddress(host, port), timeoutMillis)
            return@withContext true
          }
        } catch (e: Exception) {
          // IOException (e.g., connection refused, host unreachable, timeout)
          // SecurityException, etc.
          // println("Failed to connect to $host:$port : ${e.message}")
          return@withContext false
        }
      }
}
