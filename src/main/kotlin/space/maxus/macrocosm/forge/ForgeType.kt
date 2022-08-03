package space.maxus.macrocosm.forge

import space.maxus.macrocosm.skills.SkillType

enum class ForgeType(val skill: SkillType, val displayName: String) {
    MOLTEN(SkillType.COMBAT, "Molten Forge"),
    DEEPSLATE(SkillType.MINING, "Deepslate Foundry")
}
