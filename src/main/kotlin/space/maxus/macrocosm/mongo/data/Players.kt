package space.maxus.macrocosm.mongo.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.bson.codecs.pojo.annotations.BsonId
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.accessory.AccessoryBag
import space.maxus.macrocosm.accessory.AccessoryContainer
import space.maxus.macrocosm.collections.CollectionCompound
import space.maxus.macrocosm.forge.ActiveForgeRecipe
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.mongo.MongoRepr
import space.maxus.macrocosm.npc.shop.ShopHistory
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.PlayerEquipment
import space.maxus.macrocosm.players.PlayerMemory
import space.maxus.macrocosm.ranks.Rank
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.skills.Skills
import space.maxus.macrocosm.slayer.SlayerLevel
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.spell.essence.EssenceType
import java.math.BigDecimal
import java.util.*

data class MongoPlayerEquipment(
    val necklace: ByteArray?,
    val cloak: ByteArray?,
    val belt: ByteArray?,
    val gloves: ByteArray?
) : MongoRepr<PlayerEquipment> {
    override val actual: PlayerEquipment
        @JsonIgnore
        get() = PlayerEquipment().let {
            it.necklace = necklace?.let { v -> MacrocosmItem.deserializeFromBytes(v) }
            it.cloak = cloak?.let { v -> MacrocosmItem.deserializeFromBytes(v) }
            it.belt = belt?.let { v -> MacrocosmItem.deserializeFromBytes(v) }
            it.gloves = gloves?.let { v -> MacrocosmItem.deserializeFromBytes(v) }
            it
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MongoPlayerEquipment

        if (!necklace.contentEquals(other.necklace)) return false
        if (!cloak.contentEquals(other.cloak)) return false
        if (!belt.contentEquals(other.belt)) return false
        if (!gloves.contentEquals(other.gloves)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = necklace.contentHashCode()
        result = 31 * result + cloak.contentHashCode()
        result = 31 * result + belt.contentHashCode()
        result = 31 * result + gloves.contentHashCode()
        return result
    }
}

data class MongoOwnedPet(
    val id: String,
    val rarity: Rarity,
    val level: Int,
    val overflow: Double,
    val skin: String?
) : MongoRepr<StoredPet> {
    override val actual: StoredPet
        @JsonIgnore
        get() = StoredPet(Identifier.parse(id), rarity, level, overflow, skin?.let { Identifier.parse(it) })

}

data class MongoPlayerMemory(
    val tier6Slayers: List<String>,
    val knownPowers: List<String>
) : MongoRepr<PlayerMemory> {
    override val actual: PlayerMemory
        @JsonIgnore
        get() = PlayerMemory(
            tier6Slayers.map(Identifier::parse).toMutableList(),
            knownPowers.map(Identifier::parse).toMutableList()
        )
}

data class MongoActiveForgeRecipe(
    val id: String,
    val start: Long
) : MongoRepr<ActiveForgeRecipe> {
    override val actual: ActiveForgeRecipe
        @JsonIgnore
        get() = ActiveForgeRecipe(Identifier.parse(id), start)
}

data class MongoAccessoryContainer(
    val item: String,
    val family: String,
    val rarity: Rarity
) : MongoRepr<AccessoryContainer> {
    override val actual: AccessoryContainer
        @JsonIgnore
        get() = AccessoryContainer(Identifier.parse(item), family, rarity)

}

data class MongoAccessoryBag(
    val power: String,
    val capacity: Int,
    val redstoneSlots: Int,
    val mithrilSlots: Int,
    val jacobusSlots: Int,
    val accessories: List<MongoAccessoryContainer>
) : MongoRepr<AccessoryBag> {
    override val actual: AccessoryBag
        @JsonIgnore
        get() = AccessoryBag().let {
            it.power = Identifier.parse(power)
            it.capacity = capacity
            it.redstoneCollSlots = redstoneSlots
            it.mithrilCollSlots = mithrilSlots
            it.jacobusSlots = jacobusSlots
            it.accessories.addAll(accessories.map(MongoAccessoryContainer::actual))
            it
        }
}

data class MongoShopHistory(
    val limit: Int,
    val history: List<ByteArray>
): MongoRepr<ShopHistory> {
    override val actual: ShopHistory
        @JsonIgnore
        get() = ShopHistory(limit, history.map { bytes -> ItemStack.deserializeBytes(bytes) }.toMutableList())
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class MongoPlayerData(
    @BsonId
    val uuid: UUID,
    val equipment: MongoPlayerEquipment,
    val rank: Rank,
    val firstJoin: Long,
    val lastJoin: Long,
    val playtime: Long,
    val baseStats: Map<String, Float>,
    val purse: BigDecimal,
    val bank: BigDecimal,
    val skills: Skills,
    val collections: CollectionCompound,
    val ownedPets: HashMap<String, MongoOwnedPet>,
    val activePet: String,
    val memory: MongoPlayerMemory,
    val forge: List<MongoActiveForgeRecipe>,
    val unlockedRecipes: List<String>,
    val slayers: HashMap<SlayerType, SlayerLevel>,
    val essence: HashMap<EssenceType, Int>,
    val accessories: MongoAccessoryBag,
    val goals: List<String>,
    val shopHistory: MongoShopHistory?
) : MongoRepr<MacrocosmPlayer?> {
    override val actual: MacrocosmPlayer
        @JsonIgnore
        get() = MacrocosmPlayer.loadPlayer(this)
}
