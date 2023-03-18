package space.maxus.macrocosm.util.general

import space.maxus.macrocosm.exceptions.MacrocosmThrowable
import space.maxus.macrocosm.util.camelToSnakeCase

/**
 * A checker object
 */
object Ensure {
    fun isNull(any: Any?, message: String = "Assertion failed") {
        if (any != null)
            throw AnnotatedException(NullPointerException(), "Provided value was *not* null: $message")
    }

    fun isNotNull(any: Any?, message: String = "Assertion failed") {
        if (any == null)
            throw AnnotatedException(NullPointerException(), "Provided value was null: $message")
    }

    fun isTrue(check: Boolean, message: String = "Assertion failed") {
        if (!check)
            throw AnnotatedException(null, "Provided value was false, expected true: $message")
    }

    fun isFalse(check: Boolean, message: String = "Assertion failed") {
        if (check)
            throw AnnotatedException(null, "Provided value was true, expected false: $message")
    }

    fun isEqual(a: Any?, b: Any?, message: String = "Assertion failed") {
        if (a?.equals(b) != true)
            throw AnnotatedException(null, "Provided values were equal: $message")
    }

    fun isNotEqual(a: Any?, b: Any?, message: String = "Assertion failed") {
        if (a?.equals(b) == true)
            throw AnnotatedException(null, "Provided values weren't equal: $message")
    }

    data class AnnotatedException(val base: Exception?, val additional: String = "") : MacrocosmThrowable(
        base?.javaClass?.name?.replaceFirstChar(Char::lowercase)?.camelToSnakeCase()?.uppercase() ?: "ASSERTION_FAIL",
        "While doing a check: ${base?.message ?: "assertion failed"} (${base?.cause ?: "no cause"}); $additional"
    )
}
