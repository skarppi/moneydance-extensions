package com.moneydance.modules.features.crypto.ui

import com.moneydance.modules.features.MDApi
import com.moneydance.modules.features.crypto.model.CryptoTxn
import java.time.format.DateTimeFormatter
import java.util.*
import javax.swing.table.AbstractTableModel

class TransactionListTableModel(val api: MDApi) : AbstractTableModel() {
    private var transactions: List<CryptoTxn> = ArrayList()

    private val dateTimeFormatter = DateTimeFormatter.ofPattern(api.dateTimeFormat)

    fun setPositions(transactions: List<CryptoTxn>) {
        this.transactions = transactions
        fireTableDataChanged()
    }

    override fun getRowCount(): Int {
        return transactions.size
    }

    override fun getColumnCount(): Int {
        return 6
    }

    override fun getColumnName(column: Int): String {
        return when (column) {
            0 -> "Date"
            1 -> "Account"
            2 -> "Operation"
            3 -> "Amount"
            4 -> "Coin"
            5 -> "Remark"
            else -> ""
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val txn = transactions[rowIndex]
        return when (columnIndex) {
            0 -> dateTimeFormatter.format(txn.date)
            1 -> txn.account
            2 -> txn.operation
            3 -> txn.amount
            4 -> txn.coin
            5 -> txn.existingTxn?.toString()
            else -> null
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    fun getTooltipAt(rowIndex: Int, columnIndex: Int): String {
        return when (columnIndex) {
            else -> ""
        }
    }

    fun doubleClickAt(rowIndex: Int, columnIndex: Int) {
        val txn = transactions[rowIndex]
        when (columnIndex) {
            5 -> api.gui.showTxnInNewWindow(txn.existingTxn)
            else -> ""
        }
    }
}