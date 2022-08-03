package space.maxus.macrocosm.forge.ui

import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.kSpigotGUI
import space.maxus.macrocosm.forge.ForgeType
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.text

fun displayForge(player: MacrocosmPlayer, forge: ForgeType) = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text(forge.displayName)

}
