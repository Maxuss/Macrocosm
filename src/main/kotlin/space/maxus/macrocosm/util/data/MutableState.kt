package space.maxus.macrocosm.util.data

import kotlin.reflect.KProperty

open class MutableState<V>(var value: V) {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>) = value
    operator fun setValue(thisRef: Any?, prop: KProperty<*>, newValue: V) {
        this.value = newValue
    }
}

fun <V> mutableStateOf(value: V) = MutableState(value)
