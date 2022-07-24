package space.maxus.macrocosm.ability.types.summoning

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.monster.Spider
import net.minecraft.world.level.Level
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.nms.AbsFollowOwnerGoal
import space.maxus.macrocosm.nms.NativeMacrocosmSummon
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.generic.id
import java.util.*

object TestSummon : SummoningAbility(
    "Test Summon",
    "Summons a test entity",
    AbilityCost(100, summonDifficulty = 2),
    "test_summon",
    ::TestSummonEntity
) {
    private class TestSummonEntity(level: Level, override val owner: UUID) : Spider(EntityType.SPIDER, level),
        NativeMacrocosmSummon {
        override val delegateId: Identifier = id("test_summon")

        override fun registerGoals() {
            goalSelector.addGoal(3, FloatGoal(this))
            goalSelector.addGoal(4, AbsFollowOwnerGoal(this, 1.4, 10f, 3f, false))
            goalSelector.addGoal(4, RandomStrollGoal(this, 1.4))
        }
    }
}
