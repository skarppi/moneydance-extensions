package com.moneydance.modules.features.formula.reminder;

import com.moneydance.modules.features.formula.split.FormulaResolver;
import com.moneydance.modules.features.formula.split.FormulaSplitTxn;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.*;

public class ReminderDetailsTableModel extends AbstractTableModel {

    @Getter
    private List<FormulaSplitTxn> transactions = new ArrayList<>();

    private LocalDate nextPayment;

    @Getter
    private FormulaResolver resolver = new FormulaResolver();

    public void setTransactions(List<FormulaSplitTxn> transactions, LocalDate nextPayment) {
        this.transactions = transactions;
        this.nextPayment = nextPayment;
        this.invalidate();
    }

    private void invalidate() {
        resolver.resolve(transactions, nextPayment);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return transactions.size();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 1:
                return "Category";
            case 2:
                return "A";
            case 3:
                return "* B";
            case 4:
                return "+ C";
            case 5:
                return "Payment (+V)";
            case 6:
                return "Deposit (-V)";
            default :
                return "Name";
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FormulaSplitTxn f = transactions.get(rowIndex);

        switch (columnIndex) {
            case 1:
                return f.getTxn().getAccount().getFullAccountName();
            case 2:
                return f.getA();
            case 3:
                return f.getB();
            case 4:
                return f.getC();
            case 5:
                return f.formatValue(resolver.getValue(rowIndex), false);
            case 6:
                return f.formatValue(resolver.getValue(rowIndex), true);
            default :
                return f.toString();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return 2 <= columnIndex && columnIndex <= 4;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            return;
        }

        String value = (String)aValue;
        FormulaSplitTxn txn = transactions.get(rowIndex);

        if (columnIndex == 2) {
            txn.setA(value);
        } else if (columnIndex == 3) {
            txn.setB(value);
        } else if (columnIndex == 4) {
            txn.setC(value);
        }

        transactions.set(rowIndex, txn);

        invalidate();
    }
}