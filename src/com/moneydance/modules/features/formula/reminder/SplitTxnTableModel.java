package com.moneydance.modules.features.formula.reminder;

import com.moneydance.modules.features.formula.split.Cell;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.stream.IntStream;

public class SplitTxnTableModel extends AbstractTableModel {

    @Getter
    private List<FormulaTxn> transactions = new ArrayList<>();

    @Getter
    private HashMap<String, Object> cache = new HashMap<>();

    public void setTransactions(List<FormulaTxn> transactions) {
        this.transactions = transactions;
        this.calculate();
    }

    private void calculate() {
        cache.clear();

        // add work queue for all cells
        Queue<Cell> processingQueue = new LinkedList<>();
        IntStream.rangeClosed(1, getRowCount()).forEach(row -> {
            FormulaTxn split = transactions.get(row - 1);

            cache.put("BALANCE" + row, split.getTxn().getAccount().getBalance() / 100.0);

            Arrays.asList('A', 'B', 'C', 'V').forEach(col ->
                    processingQueue.add(Cell.builder()
                            .cell("" + col + row)
                            .col(col)
                            .row(row)
                            .txn(split)
                            .deps(new ArrayList())
                            .build())
            );
        });

        while(!processingQueue.isEmpty()) {

            Cell cell = processingQueue.poll();

            Object value = cell.evalCell();
            if (value instanceof String && ((String) value).startsWith("#NAME? ")) {
                String cellMissing = ((String) value).substring(7);

                // keep track of dependencies to prevent infinite loops
                cell.getDeps().add(cellMissing);

                if (processingQueue.stream().allMatch(c -> c.isLoop())) {
                    // all rows are in a loop, give up and show errors
                    cache.put(cell.getCell(), value);
                } else {
                    // try again later when dependency is ready
                    processingQueue.offer(cell);
                }
            } else {
                cache.put(cell.getCell(), value);
            }
        }

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
        FormulaTxn f = transactions.get(rowIndex);

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
                return f.formatValue(cache.get("V" + (rowIndex + 1)), false);
            case 6:
                return f.formatValue(cache.get("V" + (rowIndex + 1)), true);
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
        FormulaTxn txn = transactions.get(rowIndex);

        if (columnIndex == 2) {
            txn.setA(value);
        } else if (columnIndex == 3) {
            txn.setB(value);
        } else if (columnIndex == 4) {
            txn.setC(value);
        }

        transactions.set(rowIndex, txn);

        calculate();
    }
}