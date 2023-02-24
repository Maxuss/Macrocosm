package space.maxus.macrocosm.ui.components

data class Slot(val row: Int, val column: Int): ComponentSpace {
    val value = row * 9 + column
    override fun contains(slot: Int): Boolean {
        return slot == value
    }

    override fun enumerate(): List<Int> {
        return listOf(value)
    }

    companion object {
        // ROW ONE
        val RowOneSlotOne = Slot(0, 0)
        val RowOneSlotTwo = Slot(0, 1)
        val RowOneSlotThree = Slot(0, 2)
        val RowOneSlotFour = Slot(0, 3)
        val RowOneSlotFive = Slot(0, 4)
        val RowOneSlotSix = Slot(0, 5)
        val RowOneSlotSeven = Slot(0, 6)
        val RowOneSlotEight = Slot(0, 7)
        val RowOneSlotNine = Slot(0, 8)

        // ROW TWO
        val RowTwoSlotOne = Slot(1, 0)
        val RowTwoSlotTwo = Slot(1, 1)
        val RowTwoSlotThree = Slot(1, 2)
        val RowTwoSlotFour = Slot(1, 3)
        val RowTwoSlotFive = Slot(1, 4)
        val RowTwoSlotSix = Slot(1, 5)
        val RowTwoSlotSeven = Slot(1, 6)
        val RowTwoSlotEight = Slot(1, 7)
        val RowTwoSlotNine = Slot(1, 8)

        // ROW THREE
        val RowThreeSlotOne = Slot(2, 0)
        val RowThreeSlotTwo = Slot(2, 1)
        val RowThreeSlotThree = Slot(2, 2)
        val RowThreeSlotFour = Slot(2, 3)
        val RowThreeSlotFive = Slot(2, 4)
        val RowThreeSlotSix = Slot(2, 5)
        val RowThreeSlotSeven = Slot(2, 6)
        val RowThreeSlotEight = Slot(2, 7)
        val RowThreeSlotNine = Slot(2, 8)

        // ROW FOUR
        val RowFourSlotOne = Slot(3, 0)
        val RowFourSlotTwo = Slot(3, 1)
        val RowFourSlotThree = Slot(3, 2)
        val RowFourSlotFour = Slot(3, 3)
        val RowFourSlotFive = Slot(3, 4)
        val RowFourSlotSix = Slot(3, 5)
        val RowFourSlotSeven = Slot(3, 6)
        val RowFourSlotEight = Slot(3, 7)
        val RowFourSlotNine = Slot(3, 8)

        // ROW FIVE
        val RowFiveSlotOne = Slot(4, 0)
        val RowFiveSlotTwo = Slot(4, 1)
        val RowFiveSlotThree = Slot(4, 2)
        val RowFiveSlotFour = Slot(4, 3)
        val RowFiveSlotFive = Slot(4, 4)
        val RowFiveSlotSix = Slot(4, 5)
        val RowFiveSlotSeven = Slot(4, 6)
        val RowFiveSlotEight = Slot(4, 7)
        val RowFiveSlotNine = Slot(4, 8)

        // ROW SIX
        val RowSixSlotOne = Slot(5, 0)
        val RowSixSlotTwo = Slot(5, 1)
        val RowSixSlotThree = Slot(5, 2)
        val RowSixSlotFour = Slot(5, 3)
        val RowSixSlotFive = Slot(5, 4)
        val RowSixSlotSix = Slot(5, 5)
        val RowSixSlotSeven = Slot(5, 6)
        val RowSixSlotEight = Slot(5, 7)
        val RowSixSlotNine = Slot(5, 8)

        val All = LinearComponentSpace((0 until 9 * 6).toList())
        val Border = InventoryBorderSlots(1)
    }
}
