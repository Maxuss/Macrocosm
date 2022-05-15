package space.maxus.macrocosm.item.types

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityRegistry
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.damage.relativeLocation
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.entity.raycast
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.item.AbilityItem
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.runes.VanillaRune
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.id

val WITHER_SCROLL_IMPLOSION = WitherScrollAbility(
    "Implosion",
    "Deals <red>10.000 ${Statistic.DAMAGE.display}<gray> to nearby enemies.",
    AbilityCost(300, cooldown = 10)
) { player ->
    val paper = player.paper!!
    val stats = player.stats()!!

    // 0.1 int scaling
    val damage = DamageCalculator.calculateMagicDamage(10000, .1f, stats)
    for (entity in paper.location.getNearbyLivingEntities(5.0)) {
        if (entity is Player || entity is ArmorStand)
            continue

        DamageHandlers.summonDamageIndicator(entity.location, damage, DamageType.MAGIC)
        entity.macrocosm?.damage(damage, paper)
    }
    particle(Particle.EXPLOSION_HUGE) {
        amount = 8
        spawnAt(paper.location)
    }
    sound(Sound.ENTITY_GENERIC_EXPLODE) {
        pitch = 1.2f
        playFor(paper)
    }
}

val WITHER_SCROLL_WITHER_SHIELD = WitherScrollAbility(
    "Wither Shield",
    "Reduces damage taken by <red>10%<gray> for <yellow>5<gray> seconds. Also heals you for <blue>150%<gray> of your ${Statistic.CRIT_DAMAGE.display}<gray>.",
    AbilityCost(150, cooldown = 5)
) { player ->
    val paper = player.paper!!

    val success = AbilityCost(cooldown = 5).ensureRequirements(player, id("wither_shield_internal"), false)
    if (!success)
        return@WitherScrollAbility
    player.baseStats.damageReduction += 10f
    taskRunLater(5 * 20L) {
        player.baseStats.damageReduction -= 10f
    }

    val stats = player.stats()!!
    val health = stats.critDamage * 1.5f
    player.heal(health, stats)
    sound(Sound.ENTITY_ZOMBIE_VILLAGER_CURE) {
        playFor(paper)
    }
}

val WITHER_SCROLL_SHADOW_WARP = WitherScrollAbility(
    "Shadow Warp",
    "Create a spacial distortion <yellow>10<gray> blocks ahead of you that sucks all enemies around itself and detonates in <green>3s<gray>.",
    AbilityCost(300, cooldown = 10)
) { player ->
    val paper = player.paper!!
    val casted = raycast(paper, 10)
    val stats = player.stats()!!

    // 0.1 int scaling
    val damage = DamageCalculator.calculateMagicDamage(5000, .1f, stats)

    for (entity in casted.getNearbyLivingEntities(5.0)) {
        if (entity is Player || entity is ArmorStand)
            continue
        var amount = 0
        sound(Sound.ENTITY_ENDER_EYE_DEATH) {
            pitch = 0f
            volume = 2f
            playFor(paper)
        }
        task(period = 5L) {
            amount += 5
            particle(Particle.REDSTONE) {
                data = DustOptions(Color.BLACK, 2f)
                this.amount = (6..9).random()
                this.offset = Vector.getRandom()
                spawnAt(casted)
            }
            if (amount >= 60L) {
                it.cancel()
                val mc = entity.macrocosm!!
                mc.damage(damage)
                DamageHandlers.summonDamageIndicator(entity.location, damage, DamageType.MAGIC)
                sound(Sound.ENTITY_ENDERMAN_HURT) {
                    pitch = .7f
                    playFor(paper)
                }
                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(0x790001), 1.5f)
                    this.amount = (4..6).random()
                    this.offset = Vector.getRandom()
                    spawnAt(entity.location)
                }
                return@task
            }
            val velocity = casted.toVector().subtract(entity.location.toVector()).normalize().multiply(1.2f)
            entity.velocity = velocity
            val loc = velocity.relativeLocation(entity.location)
            particle(Particle.REDSTONE) {
                data = DustOptions(Color.BLACK, 1f)
                this.amount = (2..4).random()
                spawnAt(loc)
            }
        }
    }
}

val WITHER_SCROLL_WITHER_IMPACT = WitherScrollAbility(
    "Wither Impact",
    "Teleport <green>10 blocks<gray> ahead of you. Then implode, dealing <red>10.000 base ${Statistic.DAMAGE}<gray> to nearby enemies. Also applies the Wither Shield scroll ability.",
    AbilityCost(300)
) { player ->
    WITHER_SCROLL_WITHER_SHIELD.executor(player)
    val paper = player.paper!!

    val casted = raycast(paper, 10)
    paper.teleport(casted)

    WITHER_SCROLL_IMPLOSION.executor(player)
}

class WitherBlade(name: String, base: Material, stats: Statistics, rarity: Rarity = Rarity.LEGENDARY) : AbilityItem(
    ItemType.SWORD,
    name,
    rarity,
    base,
    stats,
    applicableRunes = listOf(VanillaRune.REDSTONE, VanillaRune.DIAMOND, VanillaRune.EMERALD)
) {
    fun addScroll(scroll: WitherScrollAbility) {
        if (abilities.contains(WITHER_SCROLL_WITHER_IMPACT))
            return
        abilities.add(scroll)
        if (abilities.size >= 3) {
            abilities.clear()
            abilities.add(WITHER_SCROLL_WITHER_IMPACT)
        }
    }

    override fun addExtraMeta(meta: ItemMeta) {
        meta.isUnbreakable = true
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        val list = ListTag()
        for (ability in abilities) {
            list.add(StringTag.valueOf((ability as AbilityBase).id.toString()))
        }
        cmp.put("WitherScrolls", list)
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt)
        val list = nbt.getList("WitherScrolls", 8)
        for (ability in 0 until list.size) {
            base.abilities.add(AbilityRegistry.find(Identifier.parse(list.getString(ability)))!!)
        }
        return base
    }

    @Suppress("UNCHECKED_CAST")
    override fun clone(): MacrocosmItem {
        val item = WitherBlade(ChatColor.stripColor(name.toLegacyString())!!, base, stats.clone(), rarity)
        item.enchantments = enchantments.clone() as HashMap<Enchantment, Int>
        item.reforge = reforge?.clone()
        item.rarityUpgraded = rarityUpgraded
        item.abilities.addAll(abilities)
        item.stars = stars
        return item
    }
}

class WitherScrollAbility(
    name: String,
    description: String,
    cost: AbilityCost,
    val executor: (player: MacrocosmPlayer) -> Unit
) : AbilityBase(AbilityType.RIGHT_CLICK, name, description, cost) {
    override fun registerListeners() {
        listen<PlayerRightClickEvent>(priority = EventPriority.HIGH) { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen
            executor(e.player)
        }
    }

    override fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        val tmp = mutableListOf<Component>()
        tmp.add(comp("<gold>Scroll Ability: $name <yellow><bold>RIGHT CLICK<!bold>").noitalic())
        for (desc in description.reduceToList()) {
            tmp.add(comp("<gray>$desc</gray>").noitalic())
        }
        cost!!.buildLore(tmp)
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlankOrEmpty()
        }
        lore.addAll(tmp)
        lore.add("".toComponent())
    }
}
