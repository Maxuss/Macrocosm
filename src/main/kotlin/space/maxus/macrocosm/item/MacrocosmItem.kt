package space.maxus.macrocosm.item

import net.axay.kspigot.data.nbtData
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.items.flags
import net.axay.kspigot.items.meta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.enchants.EnchantmentRegistry
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.events.AbilityCompileEvent
import space.maxus.macrocosm.item.buffs.BuffRegistry
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.buffs.PotatoBook
import space.maxus.macrocosm.item.runes.ApplicableRune
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.recipes.Ingredient
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeRegistry
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.getId
import space.maxus.macrocosm.util.putId

private fun starColor(star: Int): TextColor {
    return if (star >= 15) NamedTextColor.GREEN
    else if (star >= 10) NamedTextColor.AQUA
    else if (star >= 5) NamedTextColor.LIGHT_PURPLE
    else NamedTextColor.GOLD
}

const val MACROCOSM_TAG = "MacrocosmValues"

val ItemStack.macrocosm: MacrocosmItem? get() = ItemRegistry.toMacrocosm(this)
fun ItemStack.macrocosmTag(): CompoundTag {
    val nbt = this.nbtData
    if (nbt.contains(MACROCOSM_TAG))
        return nbt.getCompound(MACROCOSM_TAG)
    return CompoundTag()
}

interface MacrocosmItem : Ingredient {
    var stats: Statistics
    var specialStats: SpecialStatistics
    var amount: Int
    var stars: Int

    val id: Identifier
    val type: ItemType
    var name: Component
    val base: Material
    var rarity: Rarity
    var rarityUpgraded: Boolean
    var reforge: Reforge?
    val abilities: MutableList<ItemAbility>
    val enchantments: HashMap<Enchantment, Int>
    val maxStars: Int get() = 20
    val runes: HashMap<ApplicableRune, RuneState>
    val buffs: HashMap<MinorItemBuff, Int>
    var breakingPower: Int

    override fun id(): Identifier {
        return id
    }

    override fun item(): MacrocosmItem {
        return this
    }

    override fun stack(): ItemStack {
        return build() ?: ItemStack(Material.AIR)
    }

    fun buildLore(lore: MutableList<Component>) {

    }

    fun addExtraNbt(cmp: CompoundTag) {

    }

    fun addExtraMeta(meta: ItemMeta) {

    }

    fun addPotatoBooks(amount: Int) {
        buffs[PotatoBook] = (buffs[PotatoBook] ?: 0) + amount
    }

    fun reforge(ref: Reforge) {
        if (!ref.applicable.contains(this.type))
            return
        reforge = ref
    }

    fun stats(): Statistics {
        val base = stats.clone()
        val special = specialStats()
        for ((ench, level) in enchantments) {
            base.increase(ench.stats(level))
        }
        base.increase(reforge?.stats(rarity))
        for ((rune, state) in runes) {
            val (open, lvl) = state
            if (!open)
                continue
            base.increase(rune.stats(lvl))
        }
        base.multiply(1 + special.statBoost)
        // 2% boost from stars
        base.multiply(1 + (stars * .02f))
        return base
    }

    fun specialStats(): SpecialStatistics {
        val base = specialStats.clone()
        for ((ench, level) in enchantments) {
            base.increase(ench.special(level))
        }
        // 2% boost from stars
        base.multiply(1 + (stars * .02f))
        return base
    }

    fun upgradeRarity(): Boolean {
        if (rarityUpgraded) {
            return false
        }

        rarity = rarity.next()

        rarityUpgraded = true
        return true
    }

    fun buildName(): Component {
        var display = name
        if (reforge != null)
            display = comp("${reforge!!.name} ").append(display)
        if (stars <= 0)
            return display.color(rarity.color).noitalic()
        display = display.append(comp(" "))

        val starIndices = MutableList(5) { comp("") }
        for (star in 0 until stars) {
            var reducedIndex = star
            while (reducedIndex > 4) {
                reducedIndex -= 5
            }
            starIndices[reducedIndex] = comp("✪").color(starColor(star))
        }

        for (star in starIndices) {
            display = display.append(star)
        }

        return display.color(rarity.color).noitalic()
    }

    fun unlockRune(rune: ApplicableRune): Boolean {
        if (!this.runes.containsKey(rune))
            return false
        this.runes[rune] = RuneState(true, 0)
        return true
    }

    fun addRune(gem: ApplicableRune, tier: Int): Boolean {
        if (!this.runes.containsKey(gem) || !this.runes[gem]!!.open)
            return false
        this.runes[gem] = RuneState(true, tier)
        return true
    }

    /**
    Constructs base item stack differently, by default returns null
     **/
    fun alternativeCtor(): ItemStack? = null

    fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        rarityUpgraded = nbt.getBoolean("RarityUpgraded")
        if (rarityUpgraded)
            rarity = rarity.next()

        val reforge = nbt.getId("Reforge")
        if (reforge.isNotNull()) {
            reforge(ReforgeRegistry.find(reforge)!!)
        }

        val enchants = nbt.getCompound("Enchantments")
        for (k in enchants.allKeys) {
            if (k == "macrocosm:null")
                continue
            enchantments[EnchantmentRegistry.find(Identifier.parse(k))!!] = enchants.getInt(k)
        }

        this.stars = nbt.getInt("Stars")
        this.breakingPower = nbt.getInt("BreakingPower")
        val runes = nbt.getCompound("Runes")
        val associated = runes.allKeys.map { BuffRegistry.findRune(Identifier.parse(it)) }.associateWith {
            val cmp = runes.getCompound(it.id.toString()); RuneState(
            cmp.getBoolean("Open"),
            cmp.getInt("Tier")
        )
        }
        this.runes.putAll(associated)
        val buffsCmp = nbt.getCompound("Buffs")
        val buffs = buffsCmp.allKeys.map { BuffRegistry.findBuff(Identifier.parse(it)) }.associateWith { buffsCmp.getInt(it.id.toString()) }
        this.buffs.putAll(buffs)
        this.amount = from.amount
        return this
    }

    fun enchant(enchantment: Enchantment, level: Int): Boolean {
        if (!enchantment.levels.contains(level) || !enchantment.applicable.contains(type))
            return false
        enchantUnsafe(enchantment, level)
        return true
    }

    fun enchantUnsafe(enchantment: Enchantment, lvl: Int) {
        val name = EnchantmentRegistry.nameOf(enchantment)
        enchantments.filter { (ench, _) ->
            ench.conflicts.contains(Identifier.macro("all"))
        }.forEach { (ench, _) ->
            enchantments.remove(ench)
        }
        if (enchantment.conflicts.contains(Identifier.macro("all"))) {
            enchantments.filter { (ench, _) ->
                ench.name != "Telekinesis"
            }.forEach { (ench, _) ->
                enchantments.remove(ench)
            }
        } else {
            enchantments.filter { (ench, _) ->
                ench.conflicts.contains(name)
            }.forEach { (ench, _) ->
                enchantments.remove(ench)
            }
            if (enchantment is UltimateEnchantment) {
                enchantments.filter { (ench, _) ->
                    ench is UltimateEnchantment
                }.forEach { (ench, _) ->
                    enchantments.remove(ench)
                }
            }
        }
        enchantments[enchantment] = lvl
    }

    /**
     * Transfers all enchantments, reforges and other upgrades to other item
     */
    fun transfer(to: MacrocosmItem) {
        if (rarityUpgraded)
            to.upgradeRarity()
        for ((enchant, lvl) in enchantments) {
            to.enchantments[enchant] = lvl
        }
        if (reforge != null) {
            to.reforge = reforge
        }
        to.stars = stars
        to.buffs.putAll(this.buffs)
        to.runes.putAll(this.runes)
    }

    /**
     * Builds this item
     */
    @Suppress("UNCHECKED_CAST")
    fun build(player: MacrocosmPlayer? = null): ItemStack? {
        if (base == Material.AIR)
            return null

        val item = alternativeCtor() ?: ItemStack(base, 1)
        item.meta<ItemMeta> {
            // lore
            val lore = this.lore()?.toMutableList() ?: mutableListOf()

            // runes
            var gemComp = comp("")
            for ((gem, state) in runes) {
                val (open, lvl) = state
                gemComp = if (!open)
                    gemComp.append(gem.locked()).append(" ".toComponent())
                else if (lvl <= 0)
                    gemComp.append(gem.unlocked()).append(" ".toComponent())
                else
                    gemComp.append(gem.full(lvl)).append(" ".toComponent())
            }
            lore.add(gemComp.noitalic())

            // breaking power
            if (breakingPower > 0) {
                lore.add(comp("<dark_gray>Breaking Power $breakingPower").noitalic())
            }

            // stats
            val formattedStats = stats().formatSimple(this@MacrocosmItem)
            lore.addAll(formattedStats)
            if (formattedStats.isNotEmpty())
                lore.add("".toComponent())

            // enchants
            if (enchantments.isNotEmpty()) {
                val cloned = enchantments.clone() as HashMap<Enchantment, Int>
                if (cloned.size >= 6) {
                    val cmp = StringBuilder()
                    if (cloned.size >= 12) {
                        // 3 > enchants per line
                        var size = 0
                        cloned.filter { (ench, _) -> ench is UltimateEnchantment }.forEach { (ench, lvl) ->
                            cloned.remove(ench)
                            cmp.append(" ${MiniMessage.miniMessage().serialize(ench.displaySimple(lvl))}<!bold>")
                            size++
                        }
                        cloned.map { (ench, lvl) -> ench.displaySimple(lvl) }.forEach {
                            if (it is UltimateEnchantment)
                                return@forEach
                            cmp.append(" ${MiniMessage.miniMessage().serialize(it)}")
                            size++
                            if (size >= 3) {
                                cmp.append('\n')
                                size = 0
                            }
                        }
                    } else if (cloned.size >= 8) {
                        // 2 enchants per line
                        var size = 0
                        cloned.filter { (ench, _) -> ench is UltimateEnchantment }.forEach { (ench, lvl) ->
                            cloned.remove(ench)
                            cmp.append("${MiniMessage.miniMessage().serialize(ench.displaySimple(lvl))}<!bold>")
                            size++
                        }
                        cloned.map { (ench, lvl) -> ench.displaySimple(lvl) }.forEach {
                            if (it is UltimateEnchantment)
                                return@forEach
                            cmp.append(" ${MiniMessage.miniMessage().serialize(it)}")
                            size++
                            if (size >= 2) {
                                cmp.append('\n')
                                size = 0
                            }
                        }
                    } else {
                        // 1 enchant per line
                        cloned.filter { (ench, _) -> ench is UltimateEnchantment }.forEach { (ench, lvl) ->
                            cloned.remove(ench)
                            cmp.append("${MiniMessage.miniMessage().serialize(ench.displaySimple(lvl))}<!bold>\n")
                        }
                        cloned.map { (ench, lvl) -> ench.displaySimple(lvl) }.forEach {
                            if (it is UltimateEnchantment)
                                return@forEach
                            cmp.append("${MiniMessage.miniMessage().serialize(it)}\n")
                        }
                    }
                    val reduced = cmp.toString().trim().trimEnd('\n').split('\n').map { comp(it.trim()) }
                    lore.addAll(reduced)
                    lore.add("".toComponent())
                } else {
                    cloned.filter { (ench, _) -> ench is UltimateEnchantment }.forEach { (ench, lvl) ->
                        cloned.remove(ench)
                        ench.displayFancy(lore, lvl)
                    }
                    for ((ench, lvl) in enchantments) {
                        if (ench is UltimateEnchantment)
                            continue
                        ench.displayFancy(lore, lvl)
                    }
                    lore.add("".toComponent())
                }
            }

            // abilities
            for (ability in abilities) {
                val tmp = mutableListOf<Component>()
                ability.buildLore(tmp, player)
                val event = AbilityCompileEvent(this@MacrocosmItem, ability, tmp)
                event.callEvent()
                lore.addAll(event.lore)
            }

            // reforge
            reforge?.buildLore(lore)

            // extra lore
            buildLore(lore)

            // rarity
            lore.add(rarity.format(rarityUpgraded, type))

            lore(lore)

            // name
            displayName(buildName())

            // item flags
            flags(*ItemFlag.values())

            // unbreakable
            isUnbreakable = true

            // adding extra meta
            addExtraMeta(this)
        }

        // amount
        item.amount = amount

        // NBT
        val nbt = CompoundTag()

        // stats
        nbt.put("Stats", stats.compound())

        // special stats
        nbt.put("SpecialStats", specialStats.compound())

        // rarity
        nbt.putBoolean("RarityUpgraded", rarityUpgraded)
        nbt.putInt("Rarity", rarity.ordinal)

        // reforges
        if (reforge != null)
            nbt.putId("Reforge", ReforgeRegistry.nameOf(reforge!!) ?: Identifier.NULL)
        else
            nbt.putId("Reforge", Identifier.NULL)

        // enchants
        val enchants = CompoundTag()
        for ((ench, level) in enchantments) {
            enchants.putInt((EnchantmentRegistry.nameOf(ench) ?: Identifier.NULL).toString(), level)
        }
        nbt.put("Enchantments", enchants)

        // stars
        nbt.putInt("Stars", stars)

        // breaking power
        nbt.putInt("BreakingPower", breakingPower)

        // item ID
        nbt.putId("ID", id)

        // runes
        val gemsComp = CompoundTag()
        runes.forEach {
            val k = it.key.id.toString()
            val cmp = CompoundTag()
            cmp.putInt("Tier", it.value.tier)
            cmp.putBoolean("Open", it.value.open)
            gemsComp.put(k, cmp)
        }
        nbt.put("Runes", gemsComp)

        // buffs
        val buffsComp = CompoundTag()
        buffs.forEach {
            val k = it.key.id.toString()
            buffsComp.putInt(k, it.value)
        }
        nbt.put("Buffs", buffsComp)

        // adding extra nbt
        addExtraNbt(nbt)

        val nms = CraftItemStack.asNMSCopy(item)
        nms.tag?.put(MACROCOSM_TAG, nbt)
        return nms.asBukkitCopy()
    }

    fun clone(): MacrocosmItem
}
