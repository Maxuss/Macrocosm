package space.maxus.macrocosm.api

/**
 * Utility object to be called from [space.maxus.macrocosm.workarounds.AsyncLauncher]
 */
object AsyncLauncherKotlin {
    /**
     * I have no idea why everything breaks if I don't add an extra coroutine scope
     * wrapping API thread, even though it is already wrapped in [space.maxus.macrocosm.async.Threading.runAsync]
     * and another coroutine scope
     *
     * Don't even ask
     */
    @Suppress("DeferredResultUnused")
    @JvmStatic
    suspend fun loadApi() {
        spinApi().await()
    }
}
