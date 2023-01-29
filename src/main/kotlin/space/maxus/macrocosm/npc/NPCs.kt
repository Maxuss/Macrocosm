package space.maxus.macrocosm.npc

import org.bukkit.Sound
import space.maxus.macrocosm.entity.textureProfile
import space.maxus.macrocosm.npc.dsl.beginDialogue
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

enum class NPCs(val npc: MacrocosmNPC) {
    TEST_NPC(MacrocosmNPC(
        "Test NPC",
        "test_npc",
        textureProfile("ewogICJ0aW1lc3RhbXAiIDogMTY3NDk3OTI5MjE1NSwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXh1c2RldiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iMDU2YjhjN2U1MjAxYzI3MzliY2E1NTViNDA0MWYyM2Y0ZmM4MDVhMTFhM2M0Y2ZmMDIyODI2ZjE3NjEwODJlIgogICAgfQogIH0KfQ==", "EAToXjWiSuNsdlsYE5QNOwh52F2GpB8FKZxo9G0fdGypxO109BRumb8WWqwfzRMfWLDWGsRhRPGfKs2/Dx/ABngdw/Q2I36j4+iSnyPA9Kvq+8hE+wxAeckXYezNMKcbZKHpKXJVv9vZvto5qCJsRptYcrcbI/jw6/4jvDT3tN3JR7HAteAKv9kCvdycIF0w23MItDfVzJ9We6aZeIPV6q8Kp4w2E880l5bnRnLZjXxD+sBetHPt5cC7ALDXOINpUHLF+Pvgxdvbl9YpGqB8CqfMLy3PCLJpFEn4thFesNnDyaNTiRjdDErkgfA6mImkpYC97QifUgGt416LTj7fhk4l9J6of8NOw/RPAQRDfDDZPsk8cdskmq8Orag60kQ1YbAnPmIA8KekPN9eNXFCxZb/F6YZ/fDz8B9Hz2mDSyzwonX3nUh7po7MsFmOFwe7UECRucVW2k2U0nvzRUqHpYLwN4Ca9I9ngwy1MAkXXwMS1he6jW91oXdbRIrxjxDdsCtfyg4PQl/1p/Ru2ImkzBM3wwIhycfpJPKsCe4AtY0XdJcq6GlfpiHoRnt8KsQYBlFTejUZzzAB8Pfa8WUlhfcSlum1fMJ+ki71coK/HmkfZTKNLZF8LxiQcPBkxlRbSpwPFm9PaBAjw5TnOryAc/tqb/KGS6rLlHOslrcoMNg="),
        beginDialogue {
            branch(lacksGoal("npc.talk_to_test_npc_again")) then {
                say("Hello, <player_name>!")
                wait(1.5)
                playSound(Sound.ITEM_GOAT_HORN_SOUND_3, volume = 3)
                message("<rainbow>This is a test message!")
                wait(1)
                say("Forget about it")
            } otherwise {
                say("I don't want to talk with you anymore...")
            }
        }
    ))
    ;

    companion object {
        fun init() {
            Registry.NPC.delegateRegistration(values().map { id(it.name.lowercase()) to it.npc })
        }
    }

}
