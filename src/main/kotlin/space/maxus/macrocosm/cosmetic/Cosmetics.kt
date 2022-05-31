package space.maxus.macrocosm.cosmetic

import org.bukkit.Material
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.cosmetic.DyeItem
import space.maxus.macrocosm.item.cosmetic.SkullSkinItem
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.id

enum class Cosmetics(val inner: Cosmetic) {
    PURE_BLACK_DYE(Dye("<dark_gray>Pure Black", 0, Rarity.EPIC, Material.BLACK_DYE)),
    PURE_WHITE_DYE(Dye("Pure White", 0xFFFFFF, Rarity.EPIC, Material.WHITE_DYE)),

    MAXUS_HEAD_SKIN(SkullSkin(
        "Maxus Head",
        Rarity.SPECIAL,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAxNjc1NTUxNSwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc1ZDMwY2EyZDQxMjQ5ZjYwOTcyMThhMGY3YTc0ZDZiZGU0NmU1NmFjNWUxODgwYjk2Y2E3MmFjNmFhMDhhYyIKICAgIH0KICB9Cn0="
    ))

    ;

    companion object {
        fun init() {
            Registry.COSMETIC.delegateRegistration(values().map { id(it.name.lowercase()) to it.inner }) { id, cosmetic ->
                when(cosmetic) {
                    is Dye -> {
                        Registry.ITEM.register(id, DyeItem(cosmetic))
                    }
                    is SkullSkin -> {
                        Registry.ITEM.register(id, SkullSkinItem(cosmetic))
                    }
                }
            }
        }
    }
}
