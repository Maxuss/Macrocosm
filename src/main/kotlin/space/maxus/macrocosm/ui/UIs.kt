package space.maxus.macrocosm.ui

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

enum class UIs(val ui: MacrocosmUI? = null) {
    COLLECTION_MAIN,
    COLLECTION_RANKINGS,
    COLLECTION_SECTION,
    COLLECTION_SPECIFIC,

    ACCESSORY_BAG,
    JACOBUS,
    LEARN_POWER,
    THAUMATURGY,
    POWER_STONE_GUIDE

    ;

    companion object {
        fun init() {
            Registry.UI.delegateRegistration(values().map { id(it.name.lowercase()) to (it.ui ?: MacrocosmUI.NullUi) })
        }
    }
}
