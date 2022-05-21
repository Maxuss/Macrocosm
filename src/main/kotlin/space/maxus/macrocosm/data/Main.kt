package space.maxus.macrocosm.data

import org.slf4j.LoggerFactory
import space.maxus.macrocosm.enchants.Enchant
import space.maxus.macrocosm.entity.EntityValue
import space.maxus.macrocosm.fishing.SeaCreatures
import space.maxus.macrocosm.fishing.TrophyFishes
import space.maxus.macrocosm.item.Armor
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.buffs.Buffs
import space.maxus.macrocosm.item.runes.VanillaRune
import space.maxus.macrocosm.pets.PetValue
import space.maxus.macrocosm.recipes.RecipeValue
import space.maxus.macrocosm.reforge.ReforgeType
import space.maxus.macrocosm.zone.ZoneType
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists

fun main() {
    val logger = LoggerFactory.getLogger("space.maxus.macrocosm.data.Main")
    logger.info("Starting data generator")
    val dirPath = Path(System.getenv("user.dir"), "MacrocosmDatagen")
    dirPath.deleteIfExists()
    dirPath.createDirectories()
    // initializing registries

    ReforgeType.init()
    ItemValue.init()
    Enchant.init()
    RecipeValue.init()
    Armor.init()
    VanillaRune.init()
    Buffs.init()
    EntityValue.init()
    PetValue.init()
    ZoneType.init()
    SeaCreatures.init()
    TrophyFishes.init()

}
