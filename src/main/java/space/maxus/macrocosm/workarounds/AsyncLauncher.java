package space.maxus.macrocosm.workarounds;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.*;
import space.maxus.macrocosm.api.AsyncLauncherKotlin;

public class AsyncLauncher {
    public static void launchCoroutine(Function2<? super CoroutineScope, ? super Continuation<? super kotlin.Unit>, ? super Object> coroutine) {
        BuildersKt.launch(
            GlobalScope.INSTANCE,
            (CoroutineContext) Dispatchers.getDefault(),
            CoroutineStart.DEFAULT,
            coroutine
        );
    }

    public static void launchApi() {
        launchCoroutine(((coroutineScope, continuation) -> AsyncLauncherKotlin.loadApi(continuation)));
    }
}
