package com.moneydance.modules.features.formula.split

// column starting from A
enum class CellCol {
    A, B, C, V
}

data class Cell(
    val col: CellCol,
    val row: Int, // row number starting from 1
    val txn: FormulaSplitTxn,
    val deps: MutableList<String> = ArrayList()
) {
    val cell: String
        get() {
            return "" + col + row
        }

    // check if the same dependency has been tried two or more times in a row
    val isLoop: Boolean
        get() {
            val lastTwo = deps.takeLast(2)
            return lastTwo.size == 2 && lastTwo.distinct().size == 1
        }
    val script: String?
        get() = txn.getCellSource(col)
}