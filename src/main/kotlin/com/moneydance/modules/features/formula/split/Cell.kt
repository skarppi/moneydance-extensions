package com.moneydance.modules.features.formula.split

data class Cell(
    val col: Char, // column starting from A
    val row: Int, // row number starting from 1
    val txn: FormulaSplitTxn,
    val deps: List<String> = ArrayList()
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