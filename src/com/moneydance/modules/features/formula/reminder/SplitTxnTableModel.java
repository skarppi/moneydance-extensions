package com.moneydance.modules.features.formula.reminder;

import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SplitTxnTableModel extends AbstractTableModel {

    @Getter
    private List<FormulaTxn> transactions = new ArrayList<>();

    public void setTransactions(List<FormulaTxn> transactions) {
        this.transactions = transactions;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return transactions.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 1:
                return "Category";
            case 2:
                return "A";
            case 3:
                return "B";
            case 4:
                return "Payment";
            case 5:
                return "Deposit";
            default :
                return "Name";
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FormulaTxn f = transactions.get(rowIndex);

        switch (columnIndex) {
            case 1:
                return f.getTxn().getAccount().getFullAccountName();
            case 2:
                return f.getA();
            case 3:
                return f.getB();
            case 4:
                return f.formatPayment();
            case 5:
                return f.formatDeposit();
            default :
                return f.toString();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return 2 <= columnIndex && columnIndex <= 3;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            return;
        }

        String value = (String)aValue;
        FormulaTxn txn = transactions.get(rowIndex);

        if (columnIndex == 2) {
            txn.setA(value);
        } else if (columnIndex == 3) {
            txn.setB(value);
        }

        transactions.set(rowIndex, txn);

        fireTableRowsUpdated(rowIndex, rowIndex);
    }
}