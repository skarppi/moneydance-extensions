package com.moneydance.modules.features.crypto.ui

import com.moneydance.modules.features.MDApi
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
import com.moneydance.apps.md.view.gui.MDAction
import java.awt.GridBagLayout
import javax.swing.JButton
import com.moneydance.awt.GridC
import com.moneydance.modules.features.crypto.importer.BinanceImporter
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class TransactionList(private val api: MDApi) : JPanel() {
    private val tableModel = TransactionListTableModel(api)
    private val reminderTable: JTable
    private val comboBoxModel = DefaultComboBoxModel<Reminder>()

    init {
        reminderTable = object : JTable(tableModel) {
            //Implement table cell tool tips.
            override fun getToolTipText(e: MouseEvent): String {
                val p = e.point
                val row = rowAtPoint(p)
                val col = columnAtPoint(p)
                return tableModel.getTooltipAt(row, col)
            }
        }
        reminderTable.addMouseListener(object: MouseAdapter() {
            override fun mousePressed(mouseEvent: MouseEvent) {
                val point = mouseEvent.point
                val row = reminderTable.rowAtPoint(point)
                val col = reminderTable.columnAtPoint(point)

                if (mouseEvent.clickCount == 2 && reminderTable.selectedRow != -1) {
                    tableModel.doubleClickAt(row, col)
                }
            }
        });

        tableModel.setPositions(BinanceImporter(api).transactions())

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

    private fun buildButtonPanel(): JPanel {
        val importAction = MDAction.makeNonKeyedAction(
            null,
            "Import",
            "E"
        ) {
            tableModel.setPositions(BinanceImporter(api).transactions())
        }

        val buttonPanel = JPanel(GridBagLayout())
        buttonPanel.add(JButton(importAction), GridC.getc(0, 0).north())
        return buttonPanel
    }
}