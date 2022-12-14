package space.maxus.macrocosm.db

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import space.maxus.macrocosm.collections.CollectionCompound
import space.maxus.macrocosm.forge.ActiveForgeRecipe
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.PlayerEquipment
import space.maxus.macrocosm.players.PlayerMemory
import space.maxus.macrocosm.ranks.Rank
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.serde.Bytes
import space.maxus.macrocosm.skills.Skills
import space.maxus.macrocosm.slayer.SlayerLevel
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.spell.essence.EssenceType
import space.maxus.macrocosm.util.associateWithHashed
import space.maxus.macrocosm.util.ignoring
import java.math.BigDecimal

object BazaarDataTable : Table("bazaar") {
    val item = text("item").uniqueIndex()
    val orders = text("orders")

    override val primaryKey: PrimaryKey = PrimaryKey(item)
}

object PlayersTable : Table("players") {
    val uuid = uuid("uuid").uniqueIndex()
    val rank = integer("rank")
    val firstJoin = long("first_join")
    val lastJoin = long("last_join")
    val playtime = long("playtime")
    val purse = decimal("purse", 16, 1)
    val bank = decimal("bank", 16, 1)
    val memory = text("memory")
    val forge = text("forge")
    val collections = text("collections")
    val skills = text("skills")
    val recipes = text("recipes")
    val necklace = text("necklace").default("NULL")
    val cloak = text("cloak").default("NULL")
    val belt = text("belt").default("NULL")
    val gloves = text("gloves").default("NULL")
    val slayers = text("slayer_experience")
    val activePet = text("active_pet")
    val pets = text("pets")
    val essence = text("essence")

    override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}

class SqlPlayerData(
    val rank: Rank,
    val firstJoin: Long,
    val lastJoin: Long,
    val playtime: Long,
    val purse: BigDecimal,
    val bank: BigDecimal,
    val memory: PlayerMemory,
    val forge: List<ActiveForgeRecipe>,
    val collections: CollectionCompound,
    val skills: Skills,
    val recipes: List<Identifier>,
    val equipment: PlayerEquipment,
    val slayerExp: HashMap<SlayerType, SlayerLevel>,
    val activePet: String,
    val pets: HashMap<String, StoredPet>,
    val essence: HashMap<EssenceType, Int>
) {

    companion object {
        @Suppress("ReplaceWithEnumMap")
        fun fromRes(res: ResultRow): SqlPlayerData {
            val t = PlayersTable
            return SqlPlayerData(
                Rank.fromId(res[t.rank]),
                res[t.firstJoin],
                res[t.lastJoin],
                res[t.playtime],
                res[t.purse],
                res[t.bank],
                Bytes.deserializeObject(res[t.memory]) ?: PlayerMemory(mutableListOf()),
                Bytes.deserializeObject(res[t.forge]) ?: listOf(),
                CollectionCompound.deserialize(res[t.collections]),
                Skills.deserialize(res[t.skills]),
                Bytes.deserializeObject(res[t.recipes]) ?: listOf(),
                PlayerEquipment().apply {
                    necklace = MacrocosmItem.deserializeFromBytes(res[t.necklace])
                    cloak = MacrocosmItem.deserializeFromBytes(res[t.cloak])
                    belt = MacrocosmItem.deserializeFromBytes(res[t.belt])
                    gloves = MacrocosmItem.deserializeFromBytes(res[t.gloves])
                },
                Bytes.deserializeObject(res[t.slayers]) ?: hashMapOf(),
                res[t.activePet],
                Bytes.deserializeObject(res[t.pets]) ?: hashMapOf(),
                Bytes.deserializeObject(res[t.essence]) ?: EssenceType.values().toList()
                    .associateWithHashed(ignoring(0))
            )
        }
    }
}

object LimitedItemsTable : Table("limited") {
    val item = text("item").uniqueIndex()
    val amountObtained = integer("amount")

    override val primaryKey: PrimaryKey = PrimaryKey(item)
}

object StatsTable : Table("stats") {
    val uuid = uuid("uuid").uniqueIndex()
    val data = text("stats")

    override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}
