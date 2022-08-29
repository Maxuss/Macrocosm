package space.maxus.macrocosm.workarounds;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.*;
import space.maxus.macrocosm.api.AsyncLauncherKotlin;

/**
 * An asynchronous coroutine scope launcher
 */
public class AsyncLauncher {
    /**
     * Launch a single imitated kotlin coroutine scope from java
     * @param coroutine kotlin coroutine lambda to be called
     */
    public static void launchCoroutine(Function2<? super CoroutineScope, ? super Continuation<? super kotlin.Unit>, ? super Object> coroutine) {
        BuildersKt.launch(
            GlobalScope.INSTANCE,
            (CoroutineContext) Dispatchers.getDefault(),
            CoroutineStart.DEFAULT,
            coroutine
        );
    }

    /**
     * Launches Macrocosm API Spin
     */
    public static void launchApi() {
        launchCoroutine(((coroutineScope, continuation) -> AsyncLauncherKotlin.loadApi(continuation)));
    }
}
