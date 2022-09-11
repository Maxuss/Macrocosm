package space.maxus.macrocosm.util.data

import space.maxus.macrocosm.util.FnRet
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class RedirectingDelegate<T>(val redirect: FnRet<T>) {
    operator fun getValue(self: Any?, prop: KProperty<*>): T {
        return redirect()
    }
}

@Suppress("UnusedReceiverParameter")
fun <T> Delegates.redirect(redirect: FnRet<T>) = RedirectingDelegate(redirect)
