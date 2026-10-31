package app.enjoylearning504.core.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import app.enjoylearning504.core.domain.platform.ConnectivityObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

class AndroidConnectivityObserver(context: Context) : ConnectivityObserver {
    private val manager = context.getSystemService<ConnectivityManager>()!!

    override val isOnline: Flow<Boolean> = callbackFlow {
        val online = mutableSetOf<Network>()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                online += network
                trySend(true)
            }

            override fun onLost(network: Network) {
                online -= network
                trySend(online.isNotEmpty())
            }
        }
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        manager.registerNetworkCallback(request, callback)
        trySend(manager.activeNetwork?.let { manager.getNetworkCapabilities(it) }
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged().conflate()
}
