package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import com.google.common.collect.Multimap
import net.axay.kspigot.data.nbtData
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.items.customModel
import net.axay.kspigot.items.flags
import net.axay.kspigot.items.meta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.ability.types.armor.EntityKillCounterBonus
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.cosmetic.Dye
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.events.CostCompileEvent
import space.maxus.macrocosm.events.ItemCalculateStatsEvent
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.buffs.PotatoBook
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.item.runes.RuneType
import space.maxus.macrocosm.item.runes.StatRune
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.recipes.Ingredient
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.*
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.allNull
import space.maxus.macrocosm.util.annotations.PreviewFeature
import space.maxus.macrocosm.util.general.getId
import space.maxus.macrocosm.util.general.putId
import space.maxus.macrocosm.util.unreachable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import kotlin.math.min

fun colorMeta(color: Int): (ItemMeta) -> Unit = { (it as LeatherArmorMeta).setColor(Color.fromRGB(color)) }

private fun starColor(star: Int): TextColor {
    return if (star >= 15) NamedTextColor.GREEN
    else if (star >= 10) NamedTextColor.AQUA
    else if (star >= 5) NamedTextColor.LIGHT_PURPLE
    else NamedTextColor.GOLD
}

const val MACROCOSM_TAG = "MacrocosmValues"

val ItemStack.macrocosm: MacrocosmItem? get() = Items.toMacrocosm(this)
fun ItemStack.macrocosmTag(): CompoundTag {
    val nbt = this.nbtData
    if (nbt.contains(MACROCOSM_TAG))
        return nbt.getCompound(MACROCOSM_TAG)
    return CompoundTag()
}

interface MacrocosmItem : Ingredient, Clone, Identified {
    var stats: Statistics
    var specialStats: SpecialStatistics
    var amount: Int
    var stars: Int

    val type: ItemType
    var name: Component
    val base: Material
    var rarity: Rarity
    var rarityUpgraded: Boolean
    var reforge: Reforge?
    val abilities: MutableList<RegistryPointer>
    val enchantments: HashMap<Identifier, Int>
    val maxStars: Int get() = 20
    val runes: Multimap<RuneSlot, RuneState>
    val buffs: HashMap<MinorItemBuff, Int>
    var breakingPower: Int
    var dye: Dye?
    var skin: SkullSkin?
    val sellPrice: Double
        get() {
            return 1.0 + enchantments.toList()
                .sumOf { (ench, lvl) -> Registry.ENCHANT.find(ench).levels.indexOf(lvl) * 25.0 } + (stars / min(
                maxStars,
                1
            ).toDouble()) * 1000 + (if (reforge != null) 1000 else 0) + if (rarityUpgraded) 15000 else 0
        }


    var tempColor: Int? get() = null; set(_) { /* no-op */ }
    var tempSkin: String? get() = null; set(_) { /* no-op */ }

    @PreviewFeature
    var isDungeonised: Boolean get() = false; set(_) { /* no-op */ }

    @PreviewFeature
    val isDungeonisable: Boolean get() = false

    override fun id(): Identifier {
        return id
    }

    override fun item(): MacrocosmItem {
        return this
    }

    override fun stack(): ItemStack {
        return build() ?: ItemStack(Material.AIR)
    }

    fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {

    }

    fun addExtraNbt(cmp: CompoundTag) {
        cmp.putBoolean("BlockClicks", true)
    }

    fun addExtraMeta(meta: ItemMeta) {

    }

    fun addDye(dye: Dye): Boolean {
        if (!this.base.name.contains("LEATHER"))
            return false
        this.dye = dye
        return true
    }

    fun addSkin(skin: SkullSkin): Boolean {
        if (this.id != skin.target)
            return false
        this.skin = skin
        return true
    }

    fun addPotatoBooks(amount: Int) {
        buffs[PotatoBook] = (buffs[PotatoBook] ?: 0) + amount
    }

    fun reforge(ref: Reforge) {
        if (!ref.applicable.contains(this.type))
            return
        reforge = ref
    }

    fun stats(player: MacrocosmPlayer? = null): Statistics {
        val base = stats.clone()
        val special = specialStats()
        for ((ench, level) in enchantments) {
            val actualEnch = Registry.ENCHANT.find(ench)
            base.increase(actualEnch.stats(level, player))
        }
        base.increase(reforge?.stats(rarity))
        for ((_, state) in runes.entries()) {
            val (contained, tier) = state
            if (tier == 0 || contained == null)
                continue
            val r = Registry.RUNE.find(contained)
            if (r is StatRune)
                base.increase(r.baseStats.clone().apply { multiply(tier.toFloat()) })
        }
        base.multiply(1 + special.statBoost)
        // 2% boost from stars
        base.multiply(1 + (stars * .02f))

        val e = ItemCalculateStatsEvent(player, this, base)
        if (player != null) {
            e.callEvent()
        }
        return e.stats
    }

    fun specialStats(): SpecialStatistics {
        val base = specialStats.clone()
        for ((ench, level) in enchantments) {
            val actualEnch = Registry.ENCHANT.find(ench)
            base.increase(actualEnch.special(level))
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
            display = text("${reforge!!.name} ").append(display)
        if (stars <= 0)
            return display.color(rarity.color).noitalic()
        display = display.append(text(" "))

        val starIndices = MutableList(5) { text("") }
        for (star in 0 until stars) {
            var reducedIndex = star
            while (reducedIndex > 4) {
                reducedIndex -= 5
            }
            starIndices[reducedIndex] = text("⭐").color(starColor(star))
        }

        for (star in starIndices) {
            display = display.append(star)
        }

        return display.color(rarity.color).noitalic()
    }

    fun unlockRune(index: Int): Boolean {
        val (rune, state) = this.runes.entries().toList()[if (index < 0) return false else index]
        this.runes.remove(rune, state)
        this.runes.put(rune, RuneState(null, -1))
        return true
    }

    fun addRune(index: Int, rune: RuneType, tier: Int): Boolean {
        val (slot, state) = this.runes.entries().toList()[if (index < 0) return false else index]
        this.runes.remove(slot, state)
        this.runes.put(slot, RuneState(rune.id, tier))
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
            reforge(Registry.REFORGE.find(reforge))
        }

        val enchants = nbt.getCompound("Enchantments")
        for (k in enchants.allKeys) {
            if (k == "macrocosm:null")
                continue
            enchantments[Identifier.parse(k)] = enchants.getInt(k)
        }

        this.stars = nbt.getInt("Stars")
        this.breakingPower = nbt.getInt("BreakingPower")
        // clearing and reassigning runes
        this.runes.clear()
        val runes = nbt.getList("Runes", CompoundTag.TAG_COMPOUND.toInt())
        for (i in runes.indices) {
            val cmp = runes.getCompound(i)
            val state = RuneState(
                if (cmp.contains("Contained")) cmp.getId("Contained") else null,
                cmp.getInt("Tier")
            )
            val slot = RuneSlot.fromId(cmp.getId("SlotType"))
            this.runes.put(slot, state)
        }
        val buffsCmp = nbt.getCompound("Buffs")
        val buffs = buffsCmp.allKeys.map { Registry.ITEM_BUFF.find(Identifier.parse(it)) }
            .associateWith { buffsCmp.getInt(it.id.toString()) }
        this.buffs.putAll(buffs)
        if (nbt.contains("Dye")) {
            this.dye = Registry.COSMETIC.find(nbt.getId("Dye")) as Dye
        }
        if (nbt.contains("Skin")) {
            this.skin = Registry.COSMETIC.find(nbt.getId("Skin")) as SkullSkin
        }
        this.amount = from.amount

        val baseCmp =
            CraftItemStack.asNMSCopy(from).let { if (it.hasTag()) (it.tag ?: CompoundTag()) else CompoundTag() }

        if (baseCmp.contains("__TempColor"))
            this.tempColor = baseCmp.getInt("__TempColor")
        if (baseCmp.contains("__TempSkin"))
            this.tempSkin = baseCmp.getString("__TempSkin")

        return this
    }

    fun enchant(enchantment: Enchantment, level: Int): Boolean {
        if (!enchantment.levels.contains(level) || !enchantment.applicable.contains(type))
            return false
        enchantUnsafe(enchantment, level)
        return true
    }

    fun enchantUnsafe(enchantment: Enchantment, lvl: Int) {
        val name = Registry.ENCHANT.byValue(enchantment)
        enchantments.map { (Registry.ENCHANT.find(it.key) to it.key) to it.value }.filter { (ench, _) ->
            ench.first.conflicts.contains(Identifier.macro("all"))
        }.forEach { (ench, _) ->
            enchantments.remove(ench.second)
        }
        if (enchantment.conflicts.contains(Identifier.macro("all"))) {
            enchantments.filter { (ench, _) ->
                Registry.ENCHANT.find(ench).name != "Telekinesis"
            }.forEach { (ench, _) ->
                enchantments.remove(ench)
            }
        } else {
            enchantments.filter { (ench, _) ->
                Registry.ENCHANT.find(ench).conflicts.contains(name)
            }.forEach { (ench, _) ->
                enchantments.remove(ench)
            }
            if (enchantment is UltimateEnchantment) {
                enchantments.filter { (ench, _) ->
                    Registry.ENCHANT.find(ench) is UltimateEnchantment
                }.forEach { (ench, _) ->
                    enchantments.remove(ench)
                }
            }
        }
        enchantments[Registry.ENCHANT.byValue(enchantment)!!] = lvl
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
            to.reforge = reforge?.clone()
        }
        to.stars = stars
        to.buffs.putAll(this.buffs)
        to.dye = this.dye
        to.skin = this.skin
        for ((slot, state) in to.runes.entries()) {
            if (this.runes.containsKey(slot)) {
                to.runes.put(slot, state)
            }
        }
    }

    /**
     * Builds this item
     */
    @OptIn(PreviewFeature::class)
    @Suppress("UNCHECKED_CAST")
    fun build(player: MacrocosmPlayer? = null): ItemStack? {
        if (base == Material.AIR)
            return null

        val item = alternativeCtor() ?: ItemStack(base, 1)
        item.meta<ItemMeta> {
            // lore
            val lore = this.lore()?.toMutableList() ?: mutableListOf()

            // breaking power
            if (breakingPower > 0) {
                lore.add(text("<dark_gray>Breaking Power $breakingPower").noitalic())
            }

            // stats
            val formattedStats = stats(player).formatSimple(this@MacrocosmItem)
            lore.addAll(formattedStats)

            // runes
            if (runes.size() > 0) {
                var runeComp = text("")
                for ((slot, state) in runes.entries()) {
                    val (contained, tier) = state
                    runeComp = if (tier <= 0 || contained == null)
                        runeComp.append(slot.render()).append(" ".toComponent())
                    else
                        runeComp.append(Registry.RUNE.find(contained).render(tier)).append(" ".toComponent())
                }
                lore.add(runeComp.noitalic())
            }

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
                    val reduced = cmp.toString().trim().trimEnd('\n').split('\n').map { text(it.trim()) }
                    lore.addAll(reduced)
                    lore.add("".toComponent())
                } else {
                    cloned.filter { (ench, _) -> ench is UltimateEnchantment }.forEach { (ench, lvl) ->
                        cloned.remove(ench)
                        ench.displayFancy(lore, lvl)
                    }
                    for ((ench, lvl) in enchantments) {
                        val actualEnch = Registry.ENCHANT.find(ench)
                        if (actualEnch is UltimateEnchantment)
                            continue
                        actualEnch.displayFancy(lore, lvl)
                    }
                    lore.add("".toComponent())
                }
            }

            // abilities
            for (abilityRef in abilities) {
                val tmp = mutableListOf<Component>()
                val ability = abilityRef.get<MacrocosmAbility>() ?: continue
                ability.buildLore(tmp, player)
                if (ability is EntityKillCounterBonus && this@MacrocosmItem is KillStorageItem) {
                    tmp.addAll(ability.addLore(this@MacrocosmItem))
                }
                val event = CostCompileEvent(player, this@MacrocosmItem, ability.cost?.copy())
                if (player != null) {
                    // ignore accidental NPEs
                    event.callEvent()
                }
                lore.addAll(tmp)
                event.cost?.buildLore(lore)
                lore.add("".toComponent())
            }

            // reforge
            reforge?.buildLore(lore)

            // extra lore
            buildLore(player, lore)

            // rarity
            lore.add(rarity.format(rarityUpgraded, type, isDungeonised))

            lore(lore)

            // name
            displayName(buildName())

            // item flags
            flags(*ItemFlag.values())

            // unbreakable
            isUnbreakable = true

            // enchanted glint if enchanted
            if (enchantments.isNotEmpty())
                this.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)

            val model = Registry.MODEL_PREDICATES.findOrNull(id)
            if (model != null) {
                this.customModel = model.data
            }

            // adding extra meta
            addExtraMeta(this)

            if (this is LeatherArmorMeta) {
                if (tempColor != null) {
                    setColor(Color.fromRGB(tempColor!!))
                } else if (dye != null) {
                    val d = dye!!
                    lore.add(0, "".toComponent())
                    lore.add(0, text("<#${d.color.toString(16)}>${d.specialChar} ${d.name} Dye").noitalic())
                    setColor(Color.fromRGB(d.color))
                    lore(lore)
                }
            }

            if (this is SkullMeta && !allNull(tempSkin, skin)) {
                val texture = if (tempSkin != null) {
                    tempSkin!!
                } else if (skin != null) {
                    val s = skin!!
                    lore.add(0, "".toComponent())
                    lore.add(0, text("<dark_gray>${skin!!.name} Skin").noitalic())
                    lore(lore)
                    s.skin
                } else {
                    unreachable()
                }
                val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
                profile.setProperty(ProfileProperty("textures", texture))
                playerProfile = profile
            }
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
        if (dye != null)
            nbt.putId("Dye", Registry.COSMETIC.byValue(dye!!)!!)
        if (skin != null)
            nbt.putId("Skin", Registry.COSMETIC.byValue(skin!!)!!)

        // reforges
        if (reforge != null)
            nbt.putId("Reforge", Registry.REFORGE.byValue(reforge!!) ?: Identifier.NULL)
        else
            nbt.putId("Reforge", Identifier.NULL)

        // enchants
        val enchants = CompoundTag()
        for ((ench, level) in enchantments) {
            enchants.putInt(ench.toString(), level)
        }
        nbt.put("Enchantments", enchants)

        // stars
        nbt.putInt("Stars", stars)

        // breaking power
        nbt.putInt("BreakingPower", breakingPower)

        // item ID
        nbt.putId("ID", id)

        // runes
        val runeList = ListTag()
        for ((slot, state) in runes.entries().parallelStream()) {
            val runeCmp = CompoundTag()
            runeCmp.putId("SlotType", slot.id)
            runeCmp.putInt("Tier", state.tier)
            if (state.applied != null)
                runeCmp.putId("Contained", state.applied)
            runeList.add(runeCmp)
        }
        nbt.put("Runes", runeList)

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
        if (tempSkin != null)
            nms.tag?.putString("__TempSkin", tempSkin!!)
        if (tempColor != null)
            nms.tag?.putInt("__TempColor", tempColor!!)
        nms.tag?.put(MACROCOSM_TAG, nbt)
        return nms.asBukkitCopy()
    }

    override fun clone(): MacrocosmItem {
        throw IllegalStateException("Override the clone method of MacrocosmItem!")
    }

    fun serializeToBytes(player: MacrocosmPlayer?): String {
        return Base64.getEncoder().encodeToString(build(player)!!.serializeAsBytes())
    }

    companion object {
        fun deserializeFromBytes(bytes: String): MacrocosmItem? {
            return if (bytes == "NULL")
                null
            else ItemStack.deserializeBytes(Base64.getDecoder().decode(bytes)).macrocosm
        }
    }
}

fun DataInputStream.readIdentifier(): Identifier {
    val len = readInt()
    val buffer = ByteArray(len)
    read(buffer)
    return Identifier.parse(buffer.toString(Charsets.UTF_8))
}

fun DataOutputStream.writeIdentifier(id: Identifier) {
    val bytes = id.toString().encodeToByteArray()
    writeInt(bytes.size)
    write(bytes)
}

inline fun <reified V : MacrocosmItem> macrocosmItem(id: Identifier, builder: V.() -> Unit = { }): V {
    return (Registry.ITEM.find(id) as V).apply(builder)
}
