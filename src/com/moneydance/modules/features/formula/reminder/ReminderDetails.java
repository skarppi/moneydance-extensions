package com.moneydance.modules.features.formula.reminder;

import com.infinitekind.moneydance.model.*;
import com.moneydance.apps.md.view.gui.EditRemindersWindow;
import com.moneydance.apps.md.view.gui.MDAction;
import com.moneydance.awt.GridC;
import com.moneydance.modules.features.formula.MDApi;
import com.moneydance.util.UiUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReminderDetails extends JPanel {

    private MDApi api;

    private Reminder reminder;

    private ParentTxn parentTxn;

    private TxTableModel tableModel;

    private JTable txTable;

    private JLabel summaryLabel;

    private ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("py");

    public ReminderDetails(MDApi mdApi) {
        api = mdApi;

        tableModel = new TxTableModel();

        txTable = new JTable(tableModel);

        setLayout(new BorderLayout(UiUtil.DLG_HGAP, UiUtil.DLG_VGAP));
        setBorder(BorderFactory.createEmptyBorder(UiUtil.VGAP, UiUtil.HGAP,
                UiUtil.DLG_VGAP*2, UiUtil.HGAP));

        MDAction editAction = MDAction.makeNonKeyedAction(null,
                "Edit reminder",
                "E",
                evt -> EditRemindersWindow.editReminder(null, mdApi.getGUI(), reminder)
        );

        MDAction recordAction = MDAction.makeNonKeyedAction(null,
                "Record Transaction",
                "R",
                evt -> recordTransaction()
        );

        MDAction saveAction = MDAction.makeNonKeyedAction(null,
                "Store parameters as default",
                "R",
                evt -> storeSettings()
        );

        setLayout(new GridBagLayout());
        add(new JButton(editAction), GridC.getc(0, 0).west());
        add(new JButton(saveAction), GridC.getc(1, 0).west());
        add(new JButton(recordAction), GridC.getc(2, 0).east());
        add(new JScrollPane(txTable), GridC.getc(0, 1).colspan(3).wxy(1,1).fillboth());
        add(summaryPanel(), GridC.getc(2, 2).east());
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
        parentTxn = reminder != null ? reminder.getTransaction() : null;

        tableModel.transactions = IntStream.range(0, parentTxn != null ? parentTxn.getSplitCount() : 0)
                .mapToObj(i -> new FormulaTxn(parentTxn.getSplit(i)))
                .collect(Collectors.toList());

        tableModel.fireTableDataChanged();

        summaryLabel.setText(summaryText());
    }

    public void storeSettings() {
        for(int i=0; i < parentTxn.getSplitCount(); i++) {
            FormulaTxn formulaTxn = tableModel.transactions.get(i);
            formulaTxn.syncSettings();
        }
        tableModel.fireTableRowsUpdated(0, parentTxn.getSplitCount());

        reminder.syncItem();
    }

    public void recordTransaction() {
        AccountBook book = api.getBook();
        TransactionSet txns = book.getTransactionSet();

        int date = reminder.getNextOccurance(29991231);

        ParentTxn newTxn = parentTxn.duplicateAsNew();
        newTxn.setDateInt(date);
        newTxn.setTaxDateInt(date);
        newTxn.setDateEntered(System.currentTimeMillis());

        for(int i=0; i < newTxn.getSplitCount(); i++) {
            FormulaTxn formulaTxn = tableModel.transactions.get(i);
            SplitTxn split = newTxn.getSplit(i);
            split.setAmount(formulaTxn.getAmount());
            split.setDescription(formulaTxn.getDescription());
        }

        txns.addNewTxn(newTxn);
        book.refreshAccountBalances();

        reminder.setAcknowledgedInt(date);
        reminder.syncItem();
    }

    private String summaryText() {
        long value = parentTxn != null ? parentTxn.getValue() : 0;
        return String.format("Payment: %s  Deposit: %s",
                api.formatCurrency(value < 0 ? -value : 0),
                api.formatCurrency(value > 0 ? value : 0));
    }

    private JPanel summaryPanel() {
        summaryLabel = new JLabel("", SwingConstants.RIGHT);

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.add(summaryLabel, GridC.getc(0, 0).fillboth().east());
        return panel;
    }

    @Data
    private class FormulaTxn {
        private SplitTxn txn;
        private String A;
        private String B;

        private long paymentValue;
        private long depositValue;

        public FormulaTxn(SplitTxn txn) {
            this.txn = txn;

            readSettings();
        }

        private void readSettings() {
            paymentValue = txn.getValue() >= 0 ? txn.getValue() : 0;
            depositValue = txn.getValue() < 0 ? -txn.getValue() : 0;

            A = txn.getParameter("formula_a");
            B = txn.getParameter("formula_b");
        }

        public void syncSettings() {
            long value = getAmount();
            txn.setAmount(value, value);
            txn.setParameter("formula_a", A);
            txn.setParameter("formula_b", B);

            readSettings();
        }

        private String formula() {
            if (StringUtils.isNoneBlank(A)  && StringUtils.isNoneBlank(B)) {
                return String.format("(%s)*(%s)", A, B);
            } else if (StringUtils.isNoneBlank(A)) {
                return A;
            } else if (StringUtils.isNoneBlank(B)) {
                return B;
            } else {
                return null;
            }
        }

        public long calculate(long defaultValue) {
            try {
                String formula = formula();
                Object value;
                if (formula != null) {
                    value = engine.eval(formula);
                } else {
                    return defaultValue;
                }

                if (value instanceof Double) {
                    return Math.round(100 * (Double) value);
                } else if (value instanceof Float) {
                    return Math.round(100 * (Float)value);
                } else if (value instanceof Long) {
                    return 100 * (Long)value;
                } else if (value instanceof Integer) {
                    return 100 * (Integer)value;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                return 0;
            }
        }

        public String getDescription() {
            String description = txn.getDescription();
            String formula = formula();
            if (formula != null) {
                description += " ("
                        + formula.replace("(", "").replace(")", "")
                        + " )";
            }
            return description;
        }

        public long getAmount() {
            if (paymentValue > 0) {
                return calculate(paymentValue);
            } else if (depositValue > 0) {
                return calculate(depositValue);
            } else {
                return 0;
            }
        }

        public String format(long defaultValue) {
            if (defaultValue == 0) {
                return null;
            }

            long value = calculate(defaultValue);
            if (value == 0) {
                return "N/A";
            }

            if (value != defaultValue) {
                return String.format("%s (was %s)",
                        api.formatCurrency(value),
                        api.formatCurrency(defaultValue));
            } else {
                return api.formatCurrency(value);
            }
        }

        public String formatPayment() {
            return format(paymentValue);
        }

        public String formatDeposit() {
            return format(depositValue);
        }

        public String toString() {
            return txn.getDescription();
        }

        public void setA(String value) {
            A = value;
        }

        public void setB(String value) {
            B = value;
        }
    }

    private class TxTableModel extends AbstractTableModel {

        private List<FormulaTxn> transactions = new ArrayList<>();

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
                    return f.A;
                case 3:
                    return f.B;
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
}
