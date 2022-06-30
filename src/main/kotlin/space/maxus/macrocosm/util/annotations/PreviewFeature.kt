package space.maxus.macrocosm.util.annotations

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn("This is a preview feature that might not work correctly right now, or might not even work at all.")
annotation class PreviewFeature
