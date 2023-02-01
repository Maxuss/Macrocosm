package space.maxus.macrocosm.area

import com.google.common.base.Predicates
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

enum class AreaType(val area: Area, val model: AreaModel) {
    // these only contains special zones, others are just biomes mostly
    NONE(Area.impl(id("none"), Predicates.alwaysTrue()), AreaModel.impl("none", "<gray>None")),
    OVERWORLD(
        Area.impl(
            id("overworld"),
        ) { return@impl it.world.environment == org.bukkit.World.Environment.NORMAL },
        AreaModel.impl("overworld", "<green>Macrocosm")
    ),

    MY_TEST_AREA(
        Area.Null,
        AreaModel.impl("my_test_area", "<blue>Test Area", listOf(
            "Do something",
            "Maybe like testing",
            "idk really",
            "<rainbow>weeeeeeeeeeeeeeeeeeeeeeee"
        ))
    )

    ;

    companion object {
        fun init() {
            Registry.AREA.delegateRegistration(values().mapNotNull { if(it.area is Area.Null) null else id(it.name.lowercase()) to it.area })
            Registry.AREA_MODEL.delegateRegistration(values().map { id(it.name.lowercase()) to it.model })
        }
    }
}

