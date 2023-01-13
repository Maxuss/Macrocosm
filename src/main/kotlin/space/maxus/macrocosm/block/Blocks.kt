package space.maxus.macrocosm.block

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.loot.DropRarity
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.loot.custom
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.util.general.id


enum class Blocks(val block: MacrocosmBlock) {
    BLACKSTONE_ADAMANTINE_ORE(SimpleMacrocosmBlock(
        "blackstone_adamantine_ore",
        "Adamantine Ore",
        Rarity.UNOBTAINABLE,
        MacrocosmBlock.HARDNESS_DEEPSLATE * 4.3f,
        MacrocosmBlock.STEADINESS_DEEPSLATE + 3, // high end equipment
        450 to SkillType.MINING,
        ItemType.mining(),
        LootPool.of(custom(id("raw_adamantine"), DropRarity.COMMON, 1.0)),
        MacrocosmBlock.Sounds.BASALT
    )),
    DEEPSLATE_ADAMANTINE_ORE(SimpleMacrocosmBlock(
        "deepslate_adamantine_ore",
        "Adamantine Ore",
        Rarity.UNOBTAINABLE,
        MacrocosmBlock.HARDNESS_DEEPSLATE * 4.3f,
        MacrocosmBlock.STEADINESS_DEEPSLATE + 3, // high end equipment
        450 to SkillType.MINING,
        ItemType.mining(),
        LootPool.of(custom(id("raw_adamantine"), DropRarity.COMMON, 1.0)),
        MacrocosmBlock.Sounds.DEEPSLATE
    )),
    DEEPSLATE_SILVER_ORE(SimpleMacrocosmBlock(
        "deepslate_silver_ore",
        "Silver Ore",
        Rarity.RARE,
        MacrocosmBlock.HARDNESS_DEEPSLATE * 2f,
        MacrocosmBlock.STEADINESS_DEEPSLATE + 1, // mid end equipment
        90 to SkillType.MINING,
        ItemType.mining(),
        LootPool.of(custom(id("deepslate_silver_ore_block"), DropRarity.COMMON, 1.0)),
        MacrocosmBlock.Sounds.DEEPSLATE
    )),
    GEYSERITE_ORE(SimpleMacrocosmBlock(
        "geyserite_ore",
        "Geyserite Ore",
        Rarity.UNOBTAINABLE,
        MacrocosmBlock.HARDNESS_DEEPSLATE * 2.3f,
        MacrocosmBlock.STEADINESS_DEEPSLATE + 1, // mid-high end equipment
        190 to SkillType.MINING,
        ItemType.mining(),
        LootPool.of(custom(id("geyserite"), DropRarity.COMMON, 1.0, 1..2)),
        MacrocosmBlock.Sounds.DEEPSLATE
    )),
    MITHRIL_ORE(SimpleMacrocosmBlock(
        "mithril_ore",
        "Mithril Ore",
        Rarity.RARE,
        MacrocosmBlock.HARDNESS_DEEPSLATE * 3f,
        MacrocosmBlock.STEADINESS_DEEPSLATE + 2, // mid end equipment
        180 to SkillType.MINING,
        ItemType.mining(),
        LootPool.of(custom(id("mithril_ore_block"), DropRarity.COMMON, 1.0)),
        MacrocosmBlock.Sounds.DEEPSLATE
    )),
    SILVER_ORE(SimpleMacrocosmBlock(
        "silver_ore",
        "Silver Ore",
        Rarity.RARE,
        MacrocosmBlock.HARDNESS_STONE * 2f,
        MacrocosmBlock.STEADINESS_STONE + 2, // mid end equipment
        75 to SkillType.MINING,
        ItemType.mining(),
        LootPool.of(custom(id("silver_ore_block"), DropRarity.COMMON, 1.0)),
        MacrocosmBlock.Sounds.STONE
    )),
    TITANIUM_ORE(SimpleMacrocosmBlock(
        "titanium_ore",
        "Titanium Ore",
        Rarity.UNOBTAINABLE,
        MacrocosmBlock.HARDNESS_DEEPSLATE * 5f,
        MacrocosmBlock.STEADINESS_DEEPSLATE + 3, // mid end equipment
        350 to SkillType.MINING,
        ItemType.mining(),
        LootPool.of(custom(id("raw_titanium"), DropRarity.COMMON, 1.0)),
        MacrocosmBlock.Sounds.DEEPSLATE
    )),
    LUMIUM_ORE(SimpleMacrocosmBlock(
        "lumium_ore",
        "Lumium Ore",
        Rarity.UNOBTAINABLE,
        MacrocosmBlock.HARDNESS_DEEPSLATE * 3f,
        MacrocosmBlock.STEADINESS_DEEPSLATE + 2, // mid end equipment
        160 to SkillType.MINING,
        ItemType.mining(),
        LootPool.of(custom(id("raw_lumium"), DropRarity.COMMON, 1.0)),
        MacrocosmBlock.Sounds.AMETHYST
    )),
    ;
    companion object {
        fun init() {
            for(block in values()) {
                Registry.BLOCK.register(Identifier.macro(block.name.lowercase()), block.block)
            }
        }
    }
}
