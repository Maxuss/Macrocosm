package space.maxus.macrocosm.slayer

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.text.comp

enum class SlayerStatus(val display: (SlayerQuest) -> Component) {
    COLLECT_EXPERIENCE({
        comp("<yellow>${Formatting.withCommas(it.collectedExp.toBigDecimal())}<gray>/<red>${Formatting.withCommas(it.type.slayer.requiredExp[it.tier - 1].toBigDecimal())}<gray> Combat XP")
    }),
    SLAY_BOSS({
        comp("<yellow>Slay the boss!")
    }),
    FAIL({
        comp("<red>Quest failed!")
    }),
    SUCCESS({
        comp("<green>Boss slain!")
    })
}
