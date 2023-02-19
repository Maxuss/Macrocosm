package space.maxus.macrocosm.collections.ui

import net.axay.kspigot.gui.*
import org.bukkit.Material
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.text

internal fun specificCollectionUi(player: MacrocosmPlayer, ty: CollectionType): GUI<ForInventorySixByNine> =
    kSpigotGUI(GUIType.SIX_BY_NINE) {
        defaultPage = 0
        title = text("${ty.inst.name} Collection")

        page(0) {
            placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))
        }
    }
