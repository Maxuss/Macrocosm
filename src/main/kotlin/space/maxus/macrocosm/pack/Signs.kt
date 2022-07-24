package space.maxus.macrocosm.pack

enum class Signs(private val char: Char) {
    ENTITY_MARK('\uE238')
    ;

    override fun toString(): String {
        return char.toString()
    }
}
