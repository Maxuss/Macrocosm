package space.maxus.macrocosm.util.general

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.util.annotations.DevelopmentOnly
import java.util.logging.Level

@DevelopmentOnly
object Debug {
    fun dumpObjectData(obj: Any) {
        if (!Macrocosm.isInDevEnvironment)
            return

        val clazz = obj::class
        val dump = StringBuffer()
        dump.append("Class ${clazz.java.packageName}.${clazz.simpleName ?: "\$Anonymous"}: \n")
        dump.append("---------------\n")
        dump.append("METHODS:\n")
        for (member in clazz.java.declaredMethods) {
            try {
                member.isAccessible = true
                dump.append(
                    "${member.name}(${
                        member.parameters.map { "${it.name}: ${it.type.canonicalName}" }.joinToString()
                    }): ${member.returnType.canonicalName} = ${
                        if (member.parameters.isNotEmpty()) "<unknown>" else member.invoke(
                            obj
                        )
                    }\n"
                )
            } catch (e: Exception) {
                dump.append("!!! $member - UNRESOLVED\n")
            }
        }
        dump.append("---------------\n")
        dump.append("FIELDS:\n")
        for (field in clazz.java.declaredFields) {
            try {
                field.isAccessible = true
                dump.append("${field.name}: ${field.type.canonicalName} = ${field.get(obj)}\n")
            } catch (e: Exception) {
                dump.append("!!! $field - UNRESOLVED (${e.message})")
            }
        }

        Macrocosm.logger.info(dump.toString())
    }

    fun log(message: String) {
        if (!Macrocosm.isInDevEnvironment)
            return

        Macrocosm.logger.log(Level.INFO, message)
    }

    fun log(comp: Component) {
        if (!Macrocosm.isInDevEnvironment)
            return

        Macrocosm.componentLogger.info(comp)
    }

    fun log(any: Any?) {
        if (!Macrocosm.isInDevEnvironment)
            return

        Macrocosm.logger.log(Level.INFO, any.toString())
    }
}
