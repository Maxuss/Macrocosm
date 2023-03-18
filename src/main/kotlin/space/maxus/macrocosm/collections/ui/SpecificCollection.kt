package space.maxus.macrocosm.collections.ui

import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.dsl.macrocosmUi

internal fun specificCollectionUi(player: MacrocosmPlayer, ty: CollectionType): MacrocosmUI =
    macrocosmUi("collection_specific", UIDimensions.SIX_X_NINE) {
        title = "Collections ► ${ty.inst.name}"

        page {
            background()

            // TODO: collection UI
        }
    }
