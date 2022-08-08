package space.maxus.macrocosm.api

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object AsyncLauncherKotlin {
    @Suppress("DeferredResultUnused")
    @JvmStatic
    suspend fun loadApi() {
        coroutineScope {
            async {
                spinApi()
            }
        }
    }
}
