package space.maxus.macrocosm.util.annotations

import space.maxus.macrocosm.Macrocosm
import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@RequiresOptIn(
    "Elements marked by this annotation will throw an exception when ran in the production stage! Be careful!",
    RequiresOptIn.Level.ERROR
)
annotation class StrictDevelopmentOnly

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@RequiresOptIn(
    "Elements marked by this annotation will not do anything in the production stage, as opposed to the @StrictDevelopmentOnly annotation.",
    RequiresOptIn.Level.WARNING
)
annotation class DevelopmentOnly

@StrictDevelopmentOnly
object ProdCatcher {
    @StrictDevelopmentOnly
    fun catchProd(caller: KClass<*>) {
        if (!Macrocosm.isInDevEnvironment)
            throw AssertionError("Received a call from ${caller.qualifiedName} which only works in development environment!")
    }
}
