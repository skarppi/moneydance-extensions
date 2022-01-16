package com.moneydance.modules.features.formula.reminder

import com.moneydance.modules.features.formula.MDApi.Companion.disableReminder
import com.moneydance.modules.features.formula.MDApi.Companion.enableReminder
import com.moneydance.modules.features.formula.MDApi
import javax.swing.table.AbstractTableModel
import com.infinitekind.moneydance.model.Reminder
import com.infinitekind.util.CustomDateFormat
import java.util.ArrayList

class RemindersListTableModel(api: MDApi) : AbstractTableModel() {
    private val NEW_ENTRY = Reminder(null)
    private var reminders: MutableList<Reminder> = ArrayList()
    private val dateFormat: CustomDateFormat

    init {
        dateFormat = api.shortDateFormatter
    }

    fun setReminders(reminders: List<Reminder>) {
        this.reminders = reminders.toMutableList()
        fireTableDataChanged()
    }

    fun getReminder(index: Int): Reminder {
        return reminders[index]
    }

    fun insert(): Int {
        val insertIndex = reminders.size
        reminders.add(insertIndex, NEW_ENTRY)
        fireTableRowsInserted(insertIndex, insertIndex)
        return insertIndex
    }

    fun remove(index: Int) {
        val removed = reminders.removeAt(index)
        disableReminder(removed)
        fireTableRowsDeleted(index, index)
    }

    override fun getRowCount(): Int {
        return reminders.size
    }

    override fun getColumnCount(): Int {
        return 2
    }

    override fun getColumnName(column: Int): String {
        return when (column) {
            1 -> "Next payment"
            else -> "Name"
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val reminder = reminders[rowIndex]
        return when (columnIndex) {
            1 -> dateFormat.format(reminder.getNextOccurance(29991231))
            else -> reminder.description
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return columnIndex == 0 && NEW_ENTRY == reminders[rowIndex]
    }

    override fun setValueAt(aValue: Any, rowIndex: Int, columnIndex: Int) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            return
        }
        val reminder = aValue as Reminder
        enableReminder(reminder)
        reminders[rowIndex] = reminder
        fireTableRowsUpdated(rowIndex, rowIndex)
    }
}