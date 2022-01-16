package com.moneydance.modules.features.formula.reminder

import javax.swing.table.AbstractTableModel
import com.moneydance.modules.features.formula.split.FormulaSplitTxn
import java.time.LocalDate
import com.moneydance.modules.features.formula.split.FormulaResolver
import java.util.ArrayList

class ReminderDetailsTableModel : AbstractTableModel() {
    var transactions: MutableList<FormulaSplitTxn> = ArrayList()
        private set

    private var nextPayment: LocalDate? = null

    val resolver = FormulaResolver()
    fun setTransactions(transactions: MutableList<FormulaSplitTxn>, nextPayment: LocalDate?) {
        this.transactions = transactions
        this.nextPayment = nextPayment
        invalidate()
    }

    private fun invalidate() {
        resolver.resolve(transactions, nextPayment)
        fireTableDataChanged()
    }

    override fun getRowCount(): Int {
        return transactions.size
    }

    override fun getColumnCount(): Int {
        return 7
    }

    override fun getColumnName(column: Int): String {
        return when (column) {
            1 -> "Category"
            2 -> "A"
            3 -> "* B"
            4 -> "+ C"
            5 -> "Payment (+V)"
            6 -> "Deposit (-V)"
            else -> "Name"
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val f = transactions[rowIndex]
        return when (columnIndex) {
            1 -> f.txn.account.fullAccountName
            2 -> f.a
            3 -> f.b
            4 -> f.c
            5 -> f.formatValue(resolver.getValue(rowIndex), false)
            6 -> f.formatValue(resolver.getValue(rowIndex), true)
            else -> f.toString()
        }
    }

    fun getTooltipAt(rowIndex: Int, columnIndex: Int): String {
        return when (columnIndex) {
            2 -> resolver.getValue("A", rowIndex).toString()
            3 -> resolver.getValue("B", rowIndex).toString()
            4 -> resolver.getValue("C", rowIndex).toString()
            else -> ""
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex in 2..4
    }

    override fun setValueAt(aValue: Any, rowIndex: Int, columnIndex: Int) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            return
        }
        val value = aValue as String
        val txn = transactions[rowIndex]
        if (columnIndex == 2) {
            txn.a = value
        } else if (columnIndex == 3) {
            txn.b = value
        } else if (columnIndex == 4) {
            txn.c = value
        }
        transactions[rowIndex] = txn
        invalidate()
    }
}