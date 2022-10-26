package space.maxus.macrocosm

import space.maxus.macrocosm.util.data.SemanticVersion

object MacrocosmConstants {
    lateinit var API_VERSION: SemanticVersion
    lateinit var VERSION: SemanticVersion
    lateinit var CURRENT_IP: String
    var OFFLINE_MODE = false
    var DISCORD_BOT_TOKEN: String? = null
}
