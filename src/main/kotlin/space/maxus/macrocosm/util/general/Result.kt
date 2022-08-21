package space.maxus.macrocosm.util.general

class Result private constructor(val message: String?) {
    companion object {
        fun success() = Result(null)
        fun failure(message: String) = Result(message)
    }

    val isSuccess = message == null
    val isFailure = message != null
}
