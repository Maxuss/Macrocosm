package space.maxus.macrocosm.npc

import space.maxus.macrocosm.entity.textureProfile
import space.maxus.macrocosm.npc.dsl.beginDialogue
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

/**
 * Contains all pre-made NPCs
 */
enum class NPCs(val npc: MacrocosmNPC) {
    SHOPKEEPER(
        MacrocosmNPC(
            "Shopkeeper",
            "shopkeeper",
            textureProfile(
                "ewogICJ0aW1lc3RhbXAiIDogMTU5NjUxODI4MzY1NCwKICAicHJvZmlsZUlkIiA6ICJiZWNkZGIyOGEyYzg0OWI0YTliMDkyMmE1ODA1MTQyMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdFR2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZjOWJjMDFmMjk5Zjk4ZDU2NWEyN2JhMTBhMTI5MzkxNWFlOGJlZWVmYjhhNjc4NDVlMjMzMWRiZTZmZDZmZDYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                "YYQm873aDPHAom07/roqkuNfJjalmLDePSnAVHSzoJT0MCEn6V1S+eJIXtTSSFbuyMPZ62f8urtEmzm0mxiOF2Bn7g3s/5pBrmtr9GVtCCOWAMpX8gp2km7cSuF6kUtMtAyvsjrckDbd23KXLzGJj/eMFQODWcBDCrk//A7Frg8Nxdun9X9L9L8erLsuQ763TAchxXNGtsCBFrskILkaT7UJ9J42PK/FdcvfjYH1VPFTFs3nP63Ob3OIiIYtw08e7YgRxTVSVCQb3cHO5ZVBfkhIt0ZLDzCedONzITXyWP+gbz72Ll6ySX5T+CjItmoAFRgInrbKxmcWZz2eSoBAo/lyTj2s+uJfLUYbWkapi49uXi4f6v94jM7CAi4LY390YKEHe4geGBwKKB07B8wB8afsEHU7kL7j6yBF4vN+DwnbQHR8OQyZcexO+EZ+OqdxbnRZVLiQyukyVyGd2tii5Y86UIqfbiXizuoNo0zgYm17DiUpTbKdNmR6elnrvvW3fkz0BhpG4V6ImJeIJ5OijntBV61/1vCIIYH19zTIwKri4is/3lnXsFPSGp7A+VETHzuHI+blIqLGgG1MOYLJCBPFijikemLPGNHx7beFRg1G8d5zhuxnLmIDIYfIbEG0XqyWKY4mcP6NRJJ/qn+ATcVGWbJ9MbgsOa6w20a/y7Q="
            ),
            beginDialogue {
                branch(lacksGoal("npc.test_shopkeeper")) then {
                    say("sho p")
                    wait(2)
                    shop("test_shop")
                } otherwise {
                    shop("test_shop")
                }
            },
            isTemporary = true
        )
    )
    ;

    companion object {
        fun init() {
            Registry.NPC.delegateRegistration(values().map { id(it.name.lowercase()) to it.npc })
        }
    }

}
