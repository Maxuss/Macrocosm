package space.maxus.macrocosm.collections.table

interface CollectionTable {
    val table: Array<Int>

    fun itemsForLevel(level: Int): Int {
        return table[level - 1]
    }

    fun shouldLevelUp(currentLevel: Int, collected: Int): Boolean {
        if(currentLevel + 1 >= table.size)
            return false
        // currentAmount + collectedAmount >= next level
        return (if(currentLevel == 0) 0 else table[currentLevel - 1]) + collected >= table[currentLevel]
    }
}
