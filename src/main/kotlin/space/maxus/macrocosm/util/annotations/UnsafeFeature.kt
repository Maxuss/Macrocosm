package space.maxus.macrocosm.util.annotations

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn("This is an unsafe feature that is not advised to be used generally", RequiresOptIn.Level.WARNING)
annotation class UnsafeFeature
