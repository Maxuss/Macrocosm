package space.maxus.macrocosm.exceptions

@Suppress("unused")
class FormattingException(msg: String): Exception("An exception occurred while trying to format values: $msg")
