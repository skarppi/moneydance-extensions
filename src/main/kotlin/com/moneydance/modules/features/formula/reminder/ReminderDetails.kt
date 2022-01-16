package com.moneydance.modules.features.formula.reminder

import com.moneydance.modules.features.formula.MDApi.Companion.parseDate
import com.moneydance.modules.features.formula.MDApi.Companion.book
import com.moneydance.modules.features.formula.MDApi.Companion.formatCurrency
import com.moneydance.modules.features.formula.MDApi
import javax.swing.JPanel
import com.infinitekind.moneydance.model.Reminder
import javax.swing.JTable
import javax.swing.JLabel
import java.lang.RuntimeException
import java.awt.BorderLayout
import com.moneydance.util.UiUtil
import javax.swing.BorderFactory
import com.moneydance.apps.md.view.gui.MDAction
import com.moneydance.apps.md.view.gui.EditRemindersWindow
import java.awt.GridBagLayout
import javax.swing.JButton
import com.moneydance.awt.GridC
import javax.swing.JScrollPane
import com.moneydance.modules.features.formula.split.FormulaSplitTxn
import java.util.stream.Collectors
import java.awt.event.MouseEvent
import java.util.function.ToLongFunction
import javax.swing.SwingConstants

class ReminderDetails(private val api: MDApi) : JPanel() {
    private var reminder: Reminder? = null

    private val tableModel = ReminderDetailsTableModel()
    private val txTable: JTable
    private val summaryLabel = JLabel("", SwingConstants.RIGHT)

    init {
        txTable = object : JTable(tableModel) {
            //Implement table cell tool tips.
            override fun getToolTipText(e: MouseEvent): String {
                val p = e.point
                val rowIndex = rowAtPoint(p)
                val colIndex = columnAtPoint(p)
                return try {
                    tableModel.getTooltipAt(rowIndex, colIndex)
                } catch (e1: RuntimeException) {
                    //catch null pointer exception if mouse is over an empty line
                    e1.printStackTrace()
                    println(e1)
                    ""
                }
            }
        }
        layout = BorderLayout(UiUtil.DLG_HGAP, UiUtil.DLG_VGAP)
        border = BorderFactory.createEmptyBorder(
            UiUtil.VGAP, UiUtil.HGAP,
            UiUtil.DLG_VGAP * 2, UiUtil.HGAP
        )
        val editAction = MDAction.makeNonKeyedAction(
            null,
            "Edit reminder",
            "E"
        ) {
            EditRemindersWindow.editReminder(null, api.gui, reminder)
        }
        val recordAction = MDAction.makeNonKeyedAction(
            null,
            "Record Transaction",
            "R"
        ) {
            recordTransaction()
        }
        val saveAction = MDAction.makeNonKeyedAction(
            null,
            "Store parameters as default",
            "R"
        ) {
            storeSettings()
        }
        layout = GridBagLayout()
        add(JButton(editAction), GridC.getc(0, 0).west())
        add(JButton(saveAction), GridC.getc(1, 0).west())
        add(JButton(recordAction), GridC.getc(2, 0).east())
        add(JScrollPane(txTable), GridC.getc(0, 1).colspan(3).wxy(1f, 1f).fillboth())
        add(summaryPanel(), GridC.getc(2, 2).east())

        // update summary after data changes
        tableModel.addTableModelListener { summaryLabel.text = summaryText() }
    }

    private val splitCount: Int
        get() = reminder?.transaction?.splitCount ?: 0

    fun setReminder(reminder: Reminder?) {
        this.reminder = reminder

        val splits = MutableList(splitCount) { i ->
            FormulaSplitTxn(i, reminder!!.transaction.getSplit(i), tableModel.resolver)
        }
        val nextPayment = if (reminder != null) parseDate(reminder.getNextOccurance(29991231)) else null
        tableModel.setTransactions(splits, nextPayment)
    }

    fun storeSettings() {
        reminder?.let { reminder ->
            for (i in 0 until splitCount) {
                val formulaSplitTxn = tableModel.transactions[i]
                formulaSplitTxn.syncSettings()
            }
            reminder.syncItem()
            tableModel.fireTableRowsUpdated(0, splitCount)
        }
    }

    fun recordTransaction() {
        reminder?.let { reminder ->
            val txns = book.transactionSet
            val date = reminder.getNextOccurance(29991231)
            val newTxn = reminder.transaction.duplicateAsNew()
            newTxn.dateInt = date
            newTxn.taxDateInt = date
            newTxn.dateEntered = System.currentTimeMillis()
            for (i in 0 until newTxn.splitCount) {
                val formulaSplitTxn = tableModel.transactions[i]
                val split = newTxn.getSplit(i)
                split.setAmount(formulaSplitTxn.amount, formulaSplitTxn.amount)
                split.description = formulaSplitTxn.description
            }
            txns.addNewTxn(newTxn)
            reminder.setAcknowledgedInt(date)
            reminder.syncItem()
        }
    }

    private fun summaryText(): String {
        val value = tableModel.transactions.stream()
            .map { obj: FormulaSplitTxn -> obj.amount }
            .collect(Collectors.summingLong(ToLongFunction { obj: Long -> obj }))
        return String.format(
            "Payment: %s  Deposit: %s",
            formatCurrency(if (value < 0) -value else 0),
            formatCurrency(if (value > 0) value else 0)
        )
    }

    private fun summaryPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.add(summaryLabel, GridC.getc(0, 0).fillboth().east())
        return panel
    }
}