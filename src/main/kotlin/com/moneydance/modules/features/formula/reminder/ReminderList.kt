package com.moneydance.modules.features.formula.reminder

import com.moneydance.modules.features.formula.MDApi
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.DefaultComboBoxModel
import com.infinitekind.moneydance.model.Reminder
import javax.swing.ListSelectionModel
import javax.swing.JComboBox
import javax.swing.DefaultCellEditor
import java.awt.BorderLayout
import com.moneydance.util.UiUtil
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.event.ListSelectionEvent
import com.moneydance.apps.md.view.gui.MDAction
import com.moneydance.apps.md.view.gui.MDImages
import java.awt.GridBagLayout
import javax.swing.JButton
import com.moneydance.awt.GridC

class ReminderList(private val api: MDApi) : JPanel() {
    private val tableModel = RemindersListTableModel(api)
    private val reminderTable: JTable
    private val comboBoxModel = DefaultComboBoxModel<Reminder>()

    init {
        reload()
        reminderTable = JTable(tableModel)
        reminderTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        reminderTable.columnModel.getColumn(0).cellEditor = DefaultCellEditor(JComboBox(comboBoxModel))
        layout = BorderLayout(UiUtil.DLG_HGAP, UiUtil.DLG_VGAP)
        border = BorderFactory.createEmptyBorder(
            UiUtil.VGAP, UiUtil.HGAP,
            UiUtil.DLG_VGAP * 2, UiUtil.HGAP
        )
        add(JScrollPane(reminderTable), BorderLayout.CENTER)
        add(buildButtonPanel(), BorderLayout.SOUTH)
    }

    fun reload() {
        tableModel.setReminders(api.getReminders(true))
    }

    fun addSelectionListener(listener: (Reminder?) -> Unit) {
        reminderTable.selectionModel.addListSelectionListener { event: ListSelectionEvent ->
            if (!event.valueIsAdjusting) {
                listener(selectedReminder)
            }
        }
    }

    private fun buildButtonPanel(): JPanel {
        val gui = api.gui
        val addAction = MDAction.makeIconAction(
            gui,
            gui.getIcon(MDImages.PLUS)
        ) {
            comboBoxModel.removeAllElements()
            comboBoxModel.addAll(api.getReminders(false))
            if (comboBoxModel.size > 0) {
                val index = tableModel.insert()
                reminderTable.selectionModel.setSelectionInterval(index, index)
            }
        }
        val removeAction = MDAction.makeIconAction(
            gui,
            gui.getIcon(MDImages.MINUS)
        ) {
            val selectedRow = reminderTable.selectedRow
            if (selectedRow >= 0) {
                tableModel.remove(selectedRow)
                reminderTable.selectionModel.clearSelection()
            }
        }
        val buttonPanel = JPanel(GridBagLayout())
        buttonPanel.add(JButton(addAction), GridC.getc(0, 0).north())
        buttonPanel.add(JButton(removeAction), GridC.getc(1, 0).fillboth())
        return buttonPanel
    }

    private val selectedReminder: Reminder?
        get() {
            val selectedRow = reminderTable.selectedRow
            return if (selectedRow >= 0) {
                tableModel.getReminder(selectedRow)
            } else null
        }
}