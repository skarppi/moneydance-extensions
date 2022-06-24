package com.moneydance.modules.features.crypto.ui

import com.infinitekind.moneydance.model.AbstractTxn
import com.moneydance.modules.features.MDApi
import com.moneydance.modules.features.crypto.model.CryptoTxn
import com.moneydance.modules.features.crypto.services.MDInvestments
import java.time.format.DateTimeFormatter
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
        return 4
    }

    override fun getColumnName(column: Int): String {
        return when (column) {
            0 -> "Date"
            1 -> "Account"
            2 -> "Splits"
            3 -> "Existing txn"
            else -> ""
        }
    }

//    fun txnToString(txn: ParentTxn): String {
//
//    }
//

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val txn = transactions[rowIndex]
        return when (columnIndex) {
            0 -> dateTimeFormatter.format(txn.date)
            1 -> txn.account
            2 -> txn.sourceLines.map { line ->
                "${line.operation}: ${line.amount} ${line.coin}"
            }.joinToString(", ")
            3 -> {
                txn.existingTxnStatus
            }
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
            3 -> {
                //api.gui.showTxnInNewWindow(txn.existingTxn)
                //println(txn.existingTxn)

                when(txn.transferType()) {
                    AbstractTxn.TRANSFER_TYPE_BUYSELL -> MDInvestments.buySell(txn, api)
                    //AbstractTxn.TRANSFER_TYPE_BANK -> MDInvestments.transfer(txn, api)
                }
            }
            else -> ""
        }
    }
}