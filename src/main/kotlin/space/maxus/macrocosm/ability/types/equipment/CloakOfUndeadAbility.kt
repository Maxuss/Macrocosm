package space.maxus.macrocosm.ability.types.equipment

import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.ability.EquipmentAbility
import space.maxus.macrocosm.ability.types.item.isUndead
import space.maxus.macrocosm.events.EntityCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.NULL
import space.maxus.macrocosm.util.certain
import space.maxus.macrocosm.util.data.MutableContainer

object AmuletOfTheUndertakerAbility :
    EquipmentAbility("Gloomy", "Boosts your summons' ${Statistic.DEFENSE.display}<gray> by <green>+5%<gray>.") {
    override fun registerListeners() {
        listen<EntityCalculateStatsEvent> { e ->
            val id = e.self.tryRetrieveUuid() ?: return@listen
            Macrocosm.onlinePlayers.values.certain({
                it.summons.contains(id) && ensureRequirements(
                    it,
                    ItemType.NECKLACE,
                    ItemType.BELT
                )
            }) {
                e.stats.defense *= 1.05f
            }
        }
    }
}

object DeadMansBootsAbility : AbilityBase(
    AbilityType.PASSIVE,
    "Dead Gait",
    "Your summons have the same ${Statistic.SPEED.display}<gray> and ${Statistic.TRUE_DEFENSE.display}<gray> as you."
) {
    override fun registerListeners() {
        listen<EntityCalculateStatsEvent> { e ->
            val id = e.self.tryRetrieveUuid() ?: return@listen
            Macrocosm.onlinePlayers.values.certain({
                it.summons.contains(id) && ensureRequirements(
                    it,
                    EquipmentSlot.FEET
                )
            }) {
                e.stats.speed = it.stats()!!.speed
                e.stats.trueDefense = it.stats()!!.trueDefense
            }
        }
    }
}

object VampiresCowlAbility : EquipmentAbility(
    "Shadow Mantle",
    "Your summons deal <red>+300% ${Statistic.DAMAGE.display}<gray> if you haven't dealt any damage by yourself in the past <green>10 seconds<gray>."
) {
    private val players = MutableContainer.empty<Long>()

    override fun registerListeners() {
        task(period = 40L, delay = 10L) {
            players.iterMut { p ->
                p + 40L
            }
        }
        listen<PlayerJoinEvent>(priority = EventPriority.LOWEST) {
            players[it.player.uniqueId] = 0L
        }
        listen<PlayerDealDamageEvent> { e ->
            players.takeMut(e.player.ref) {
                0
            }
        }
        listen<EntityCalculateStatsEvent> { e ->
            val id = e.self.tryRetrieveUuid() ?: return@listen
            Macrocosm.onlinePlayers.values.certain({
                it.summons.contains(id) && ensureRequirements(
                    it,
                    ItemType.CLOAK
                )
            }) {
                players.take(it.ref) { dur ->
                    if (dur / 20L >= 10L)
                        e.stats.damage *= 4f
                }
            }
        }
    }
}

object CloakOfUndeadKing1 : EquipmentAbility(
    "Undead Lord",
    "You take <green>25%<gray> less ${Statistic.DAMAGE.display}<gray> from <blue>Undead<gray> when you have <green>at least 4<gray> summons nearby."
) {
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            if (!ensureRequirements(e.player, ItemType.CLOAK) || e.player.summons.size < 4)
                return@listen
            if (!e.damager.isUndead())
                return@listen
            e.damage *= .75f
        }
    }
}

object CloakOfUndeadKing2 : EquipmentAbility(
    "Dark Arts of Necromancy",
    "Boosts <green>all<gray> of your summons' stats by <green>25%<gray> for <green>5 seconds<gray> when you kill an <blue>Undead<gray>."
) {
    private val killed = MutableContainer.trulyEmpty()

    override fun registerListeners() {
        listen<PlayerKillEntityEvent> { e ->
            if (!ensureRequirements(e.player, ItemType.CLOAK) || !e.killed.isUndead())
                return@listen
            killed[e.player.ref] = NULL

            taskRunLater(5 * 20L) {
                killed.remove(e.player.ref)
            }
        }
        listen<EntityCalculateStatsEvent> { e ->
            val id = e.self.tryRetrieveUuid() ?: return@listen
            Macrocosm.onlinePlayers.values.certain({
                it.summons.contains(id) && ensureRequirements(
                    it,
                    ItemType.CLOAK
                )
            }) {
                killed.take(it.ref) {
                    e.stats.multiply(1.25f)
                }
            }
        }
    }
}
