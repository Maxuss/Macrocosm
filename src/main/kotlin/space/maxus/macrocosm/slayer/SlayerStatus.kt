package space.maxus.macrocosm.slayer

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.text.text

enum class SlayerStatus(val display: (SlayerQuest) -> Component) {
    COLLECT_EXPERIENCE({
        text("<yellow>${Formatting.withCommas(it.collectedExp.toBigDecimal())}<gray>/<red>${Formatting.withCommas(it.type.slayer.requiredExp[it.tier - 1].toBigDecimal())}<gray> Combat XP")
    }),
    SLAY_BOSS({
        text("<yellow>Slay the boss!")
    }),
    FAIL({
        text("<red>Quest failed!")
    }),
    SUCCESS({
        text("<green>Boss slain!")
    })
}
