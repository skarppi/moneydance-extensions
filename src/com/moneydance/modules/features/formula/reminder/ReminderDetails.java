package com.moneydance.modules.features.formula.reminder;

import com.infinitekind.moneydance.model.ParentTxn;
import com.infinitekind.moneydance.model.Reminder;
import com.infinitekind.moneydance.model.Txn;
import com.moneydance.modules.features.formula.MDApi;
import com.moneydance.util.UiUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReminderDetails extends JPanel {

    private MDApi api;

    private TxTableModel tableModel;

    private JTable reminderTable;

    public ReminderDetails(MDApi mdApi) {
        api = mdApi;

        tableModel = new TxTableModel();

        reminderTable = new JTable(tableModel);

        setLayout(new BorderLayout(UiUtil.DLG_HGAP, UiUtil.DLG_VGAP));
        setBorder(BorderFactory.createEmptyBorder(UiUtil.VGAP, UiUtil.HGAP,
                UiUtil.DLG_VGAP*2, UiUtil.HGAP));

        add(new JScrollPane(reminderTable), BorderLayout.CENTER);
    }

    public void setReminder(Reminder reminder) {
        ParentTxn parentTxn = reminder.getTransaction();

        this.tableModel.transactions = IntStream.range(0, parentTxn.getSplitCount())
                .mapToObj(i -> parentTxn.getSplit(i))
                .collect(Collectors.toList());
        this.tableModel.fireTableDataChanged();
    }

    private class TxTableModel extends AbstractTableModel {

        private final Reminder NEW_ENTRY = new Reminder(null);

        private List<Txn> transactions = new ArrayList<>();

        @Override
        public int getRowCount() {
            return transactions.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                default :
                    return "Name";
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Txn reminder = transactions.get(rowIndex);
            switch (columnIndex) {
                default :
                    return reminder.getDescription();
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0 && NEW_ENTRY.equals(transactions.get(rowIndex));
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (!isCellEditable(rowIndex, columnIndex)) {
                return;
            }
            Txn transaction = (Txn)aValue;
//            reminder.setDescription(reminder.getDescription() + "1");
//            transaction.syncItem();

            transactions.set(rowIndex, transaction);

            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}
