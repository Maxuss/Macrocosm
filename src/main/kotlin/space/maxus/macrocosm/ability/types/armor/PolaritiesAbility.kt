package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import org.bukkit.entity.Player
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.general.varargs
import space.maxus.macrocosm.util.getArmorIds

object BlessedPolaritiesAbility: FullSetBonus(
    "Blessed Polarities",
    "You are blessed by the <gradient:#8B45F7:#5033BF>Idols of Planes</gradient>!<br><gold>All<gray> your stats are increased by <green>3%<gray> for every player with the <dark_purple>Polarity Set<gray> of <red>Chaotic<gray> or <red>Evil<gray> <aqua>alignment<gray>.<br>" +
        "<aqua>Polarity Alignments: <aqua><br>" +
        "<blue>Order: <green>Devotedly Good<br>" +
        "<blue>Earth: <green>Lawful <yellow>Neutral<br>" +
        "<blue>Fire: <green>Lawful <red>Evil<br>" +
        "<blue>Downpour: <yellow>Neutral <green>Good<br>" +
        "<blue>Typhoon: <yellow>Devotedly Neutral<br>" +
        "<blue>Blood: <yellow>Neutral <red>Evil<br>" +
        "<blue>Spirits: <red>Chaotic <green>Good<br>" +
        "<blue>Void: <red>Chaotic <yellow>Neutral<br>" +
        "<blue>Chaos: <red>Devotedly Evil<br>" +
        "<blue>Convergence: <rainbow>Original Plane</rainbow><br>"
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureSetRequirement(e.player))
                return@listen
            val p = e.player.paper ?: return@listen
            val mod = 1f + PolarityAlignment.getNearbyAlignments(p, 8.0).map { (_, b) ->
                val (leftHand, rightHand) = b
                // find chaotic or evil
                if(leftHand == PolarityAlignment.CHAOTIC && rightHand == PolarityAlignment.EVIL) { .08f } else (if(leftHand == PolarityAlignment.CHAOTIC) 0.03f else 0f) + (if(rightHand == PolarityAlignment.EVIL) 0.03f else 0f)
            }.sum()
            e.stats.multiply(mod)
        }
    }
}

object CursedPolaritiesAbility: FullSetBonus(
    "Cursed Polarities",
    "You are blessed by the <gradient:#8B45F7:#5033BF>Idols of Planes</gradient>!<br><gold>All<gray> your stats are increased by <green>3%<gray> for every player with the <dark_purple>Polarity Set<gray> of <green>Lawful<gray> or <green>Good<gray> <aqua>alignment<gray>.<br>" +
        "<aqua>Polarity Alignments: <aqua><br>" +
        "<blue>Order: <green>Devotedly Good<br>" +
        "<blue>Earth: <green>Lawful <yellow>Neutral<br>" +
        "<blue>Fire: <green>Lawful <red>Evil<br>" +
        "<blue>Downpour: <yellow>Neutral <green>Good<br>" +
        "<blue>Typhoon: <yellow>Devotedly Neutral<br>" +
        "<blue>Blood: <yellow>Neutral <red>Evil<br>" +
        "<blue>Spirits: <red>Chaotic <green>Good<br>" +
        "<blue>Void: <red>Chaotic <yellow>Neutral<br>" +
        "<blue>Chaos: <red>Devotedly Evil<br>" +
        "<blue>Convergence: <rainbow>Original Plane</rainbow><br>"
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureSetRequirement(e.player))
                return@listen
            val p = e.player.paper ?: return@listen
            val mod = 1f + PolarityAlignment.getNearbyAlignments(p, 8.0).map { (_, b) ->
                val (leftHand, rightHand) = b
                // find lawful or good
                if(leftHand == PolarityAlignment.LAWFUL && rightHand == PolarityAlignment.GOOD) { .08f } else (if(leftHand == PolarityAlignment.LAWFUL) 0.03f else 0f) + (if(rightHand == PolarityAlignment.GOOD) 0.03f else 0f)
            }.sum()
            e.stats.multiply(mod)
        }
    }
}

object ForbiddenPolaritiesAbility: FullSetBonus(
    "Forbidden Polarities",
    "You are blessed by the <gradient:#8B45F7:#5033BF>Idols of Planes</gradient>!<br><gold>All<gray> your stats are increased by <green>1.5%<gray> for every player with the <dark_purple>Polarity Set<gray> not of <yellow>Neutral <aqua>alignment<gray>.<br>" +
        "<aqua>Polarity Alignments: <aqua><br>" +
        "<blue>Order: <green>Devotedly Good<br>" +
        "<blue>Earth: <green>Lawful <yellow>Neutral<br>" +
        "<blue>Fire: <green>Lawful <red>Evil<br>" +
        "<blue>Downpour: <yellow>Neutral <green>Good<br>" +
        "<blue>Typhoon: <yellow>Devotedly Neutral<br>" +
        "<blue>Blood: <yellow>Neutral <red>Evil<br>" +
        "<blue>Spirits: <red>Chaotic <green>Good<br>" +
        "<blue>Void: <red>Chaotic <yellow>Neutral<br>" +
        "<blue>Chaos: <red>Devotedly Evil<br>" +
        "<blue>Convergence: <rainbow>Original Plane</rainbow><br>"
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureSetRequirement(e.player))
                return@listen
            val p = e.player.paper ?: return@listen
            val alignments = PolarityAlignment.getNearbyAlignments(p, 8.0).filter { (_, b) -> b.first != PolarityAlignment.NEUTRAL }
            val mod = 1f + (alignments.size * 0.015f)
            e.stats.multiply(mod)
        }
    }
}

object EnvoyOfPolarities: FullSetBonus(
    "Envoy of the Polarities",
    "You increase <green>all<gray> stats of players within <yellow>10 blocks<gray> that wear a <dark_purple>Polarity Set<gray> by <green>3%<gray>!"
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            val p = e.player.paper ?: return@listen
            val alignment = PolarityAlignment.alignmentOfPlayer(p)
            if(alignment == null || alignment.first == PolarityAlignment.CONVERGENCE)
                return@listen
            val confluxNearby = PolarityAlignment.getNearbyAlignments(p, 10.0).filter { (_, b) -> b.first == PolarityAlignment.CONVERGENCE }.isNotEmpty()
            if(confluxNearby)
                e.stats.multiply(1.03f)
        }
    }
}

object PolarityOriginsAbility: FullSetBonus(
    "Convergence of the Planes",
    "You are truly blessed by the <gradient:#8B45F7:#5033BF>Idols of Planes</gradient>!<br><gold>All<gray> your stats are increased by <green>3.5%<gray> for every player with the <dark_purple>Polarity Set<gray> not of <rainbow>Convergence<gray> <aqua>alignment<gray> within <yellow>8 blocks<gray>.<br>" +
        "<aqua>Polarity Alignments: <aqua><br>" +
        "<blue>Order: <green>Devotedly Good<br>" +
        "<blue>Earth: <green>Lawful <yellow>Neutral<br>" +
        "<blue>Fire: <green>Lawful <red>Evil<br>" +
        "<blue>Downpour: <yellow>Neutral <green>Good<br>" +
        "<blue>Typhoon: <yellow>Devotedly Neutral<br>" +
        "<blue>Blood: <yellow>Neutral <red>Evil<br>" +
        "<blue>Spirits: <red>Chaotic <green>Good<br>" +
        "<blue>Void: <red>Chaotic <yellow>Neutral<br>" +
        "<blue>Chaos: <red>Devotedly Evil<br>" +
        "<blue>Convergence: <rainbow>Original Power</rainbow><br>"
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureSetRequirement(e.player))
                return@listen
            val p = e.player.paper ?: return@listen
            val alignments = PolarityAlignment.getNearbyAlignments(p, 8.0).filter { (_, b) -> b.first != PolarityAlignment.CONVERGENCE }
            val mod = 1f + (alignments.size * 0.035f)
            e.stats.multiply(mod)
        }
    }
}

private enum class PolarityAlignment(vararg sets: String) {
    LAWFUL("order", "earth", "fire"),
    GOOD("order", "water", "spirits"),
    NEUTRAL("earth", "water", "air", "blood", "void"),
    CHAOTIC("spirits", "void", "chaos"),
    EVIL("fire", "blood", "chaos"),

    CONVERGENCE("conflux")

    ;

    val sets by varargs(sets)

    companion object {
        fun alignmentOfPlayer(player: Player, set: List<Identifier> = player.getArmorIds()): Pair<PolarityAlignment, PolarityAlignment>? {
            if(set.size != 4)
                return null
            return PolarityAlignment.values().filter { alignment ->
                alignment.sets.firstOrNull { alignSet -> set.all { piece -> piece.path.contains(alignSet) } }?.let { true } ?: false
            }.let { (if(it.isEmpty()) return null else if(it.size == 1) /* We got neutral or conflux */ it[0] to it[0] else it[0] to it[1]) }
        }

        fun getNearbyAlignments(around: Player, radius: Double): HashMap<Player, Pair<PolarityAlignment, PolarityAlignment>> {
            val armorIds = around.getNearbyEntities(radius, radius, radius).filterIsInstance<Player>().mapNotNull { val ids = it.getArmorIds(); if(ids.size != 4) null else it to ids }
            return HashMap(armorIds.mapNotNull { (player, set) ->
                val alignments = alignmentOfPlayer(player, set) ?: return@mapNotNull null
                player to alignments
            }.associate { it })
        }
    }
}

internal fun exemptsEssence(name: String) = "<br><yellow>This set exempts the <italic>$name</italic><br><yellow>essence."
