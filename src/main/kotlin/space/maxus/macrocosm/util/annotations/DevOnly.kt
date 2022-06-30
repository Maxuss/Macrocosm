package space.maxus.macrocosm.util.annotations

import space.maxus.macrocosm.Macrocosm
import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@RequiresOptIn("Elements marked by this annotation will throw an exception when ran in production! Be careful!", RequiresOptIn.Level.WARNING)
annotation class DevOnly

@DevOnly
object ProdCatcher {
    @DevOnly
    fun catchProd(caller: KClass<*>) {
        if(!Macrocosm.isInDevEnvironment)
            throw AssertionError("Received a call from ${caller.qualifiedName} which only works in development environment!")
    }
}
