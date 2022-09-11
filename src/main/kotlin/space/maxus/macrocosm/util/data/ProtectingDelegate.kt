package space.maxus.macrocosm.util.data

import kotlin.properties.Delegates
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ProtectingDelegate<T>(
    var value: T,
    val allowed: Class<*>,
    val checkSet: Boolean = true,
    val checkGet: Boolean = false
) {
    companion object {
        private fun getCallerClass(): Class<*> {
            // thread -> [0]
            // self -> [1]
            // previous caller -> [2]
            // caller of the caller -> [3] (what we need)
            return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk { it.toList() }[3].declaringClass
        }
    }

    operator fun getValue(self: Any?, property: KProperty<*>): T {
        if (checkGet && getCallerClass().canonicalName.let { it == null || it != allowed.canonicalName }) {
            throw IllegalAccessException("This protecting delegate only allows read access from ${allowed.canonicalName} class!")
        }
        return value
    }

    operator fun setValue(self: Any?, property: KProperty<*>, value: T) {
        if (checkSet && getCallerClass().canonicalName.let { it == null || it != allowed.canonicalName }) {
            throw IllegalAccessException("This protecting delegate only allows write access from ${allowed.canonicalName} class!")
        }
        this.value = value
    }
}

@Suppress("UnusedReceiverParameter")
fun <T> Delegates.protecting(value: T, lock: KClass<*>, checkRead: Boolean = false, checkWrite: Boolean = true) =
    ProtectingDelegate(value, lock.java, checkWrite, checkRead)
