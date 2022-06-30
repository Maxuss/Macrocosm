package space.maxus.macrocosm.util.generic

import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.util.annotations.DevOnly
import space.maxus.macrocosm.util.annotations.ProdCatcher

@DevOnly
object Debug {
    @DevOnly
    fun dumpObjectData(obj: Any) {
        ProdCatcher.catchProd(this::class)

        val clazz = obj::class
        val dump = StringBuffer()
        dump.append("Class ${clazz.java.packageName}.${clazz.simpleName ?: "\$Anonymous"}: \n")
        dump.append("---------------\n")
        dump.append("METHODS:\n")
        for(member in clazz.java.declaredMethods) {
            try {
                member.isAccessible = true
                dump.append("${member.name}(${member.parameters}): ${member.returnType.canonicalName} = ${if(member.parameters.isNotEmpty()) "<unknown>" else member.invoke(obj)}\n")
            } catch (e: Exception) {
                dump.append("!!! $member - UNRESOLVED\n")
            }
        }
        dump.append("---------------\n")
        dump.append("FIELDS:\n")
        for(field in clazz.java.declaredFields) {
            try {
                field.isAccessible = true
                dump.append("${field.name}: ${field.type.canonicalName} = ${field.get(obj)}\n")
            } catch (e: Exception) {
                dump.append("!!! $field - UNRESOLVED")
            }
        }

        Macrocosm.logger.info(dump.toString())
    }
}
