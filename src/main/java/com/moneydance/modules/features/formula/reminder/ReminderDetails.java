package com.moneydance.modules.features.formula.reminder;

import com.infinitekind.moneydance.model.*;
import com.moneydance.apps.md.view.gui.EditRemindersWindow;
import com.moneydance.apps.md.view.gui.MDAction;
import com.moneydance.awt.GridC;
import com.moneydance.modules.features.formula.MDApi;
import com.moneydance.modules.features.formula.split.FormulaResolver;
import com.moneydance.modules.features.formula.split.FormulaSplitTxn;
import com.moneydance.util.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReminderDetails extends JPanel {

    private MDApi api;

    private Reminder reminder;

    private ParentTxn parentTxn;

    private ReminderDetailsTableModel tableModel;

    private JTable txTable;

    private JLabel summaryLabel;

    public ReminderDetails(MDApi mdApi) {
        api = mdApi;

        tableModel = new ReminderDetailsTableModel();

        txTable = new JTable(tableModel) {
            //Implement table cell tool tips.
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    return tableModel.getTooltipAt(rowIndex, colIndex);
                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                    e1.printStackTrace();
                    System.out.println(e1);
                }
                return null;
            }
        };

        setLayout(new BorderLayout(UiUtil.DLG_HGAP, UiUtil.DLG_VGAP));
        setBorder(BorderFactory.createEmptyBorder(UiUtil.VGAP, UiUtil.HGAP,
                UiUtil.DLG_VGAP*2, UiUtil.HGAP));

        MDAction editAction = MDAction.makeNonKeyedAction(null,
                "Edit reminder",
                "E",
                evt -> EditRemindersWindow.editReminder(null, mdApi.getGui(), reminder)
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

        // update summary after data changes
        tableModel.addTableModelListener(e -> summaryLabel.setText(summaryText()));
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
        parentTxn = reminder != null ? reminder.getTransaction() : null;

        int splitCount = parentTxn != null ? parentTxn.getSplitCount() : 0;

        LocalDate nextPayment = reminder != null ?
                MDApi.parseDate(reminder.getNextOccurance(29991231)) :
                null;

        FormulaResolver resolver = tableModel.getResolver();

        tableModel.setTransactions(IntStream.range(0, splitCount)
                .mapToObj(i -> new FormulaSplitTxn(i, parentTxn.getSplit(i), resolver))
                .collect(Collectors.toList()), nextPayment);
    }

    public void storeSettings() {
        for(int i=0; i < parentTxn.getSplitCount(); i++) {
            FormulaSplitTxn formulaSplitTxn = tableModel.getTransactions().get(i);
            formulaSplitTxn.syncSettings();
        }
        reminder.syncItem();

        tableModel.fireTableRowsUpdated(0, parentTxn.getSplitCount());
    }

    public void recordTransaction() {
        TransactionSet txns = api.getBook().getTransactionSet();

        int date = reminder.getNextOccurance(29991231);

        ParentTxn newTxn = parentTxn.duplicateAsNew();
        newTxn.setDateInt(date);
        newTxn.setTaxDateInt(date);
        newTxn.setDateEntered(System.currentTimeMillis());

        for(int i=0; i < newTxn.getSplitCount(); i++) {
            FormulaSplitTxn formulaSplitTxn = tableModel.getTransactions().get(i);
            SplitTxn split = newTxn.getSplit(i);
            split.setAmount(formulaSplitTxn.getAmount(), formulaSplitTxn.getAmount());
            split.setDescription(formulaSplitTxn.getDescription());
        }

        txns.addNewTxn(newTxn);

        reminder.setAcknowledgedInt(date);
        reminder.syncItem();
    }

    private String summaryText() {
        long value = tableModel.getTransactions().stream()
                .map(FormulaSplitTxn::getAmount)
                .collect(Collectors.summingLong(Long::longValue));
        return String.format("Payment: %s  Deposit: %s",
                MDApi.formatCurrency(value < 0 ? -value : 0),
                MDApi.formatCurrency(value > 0 ? value : 0));
    }

    private JPanel summaryPanel() {
        summaryLabel = new JLabel("", SwingConstants.RIGHT);

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.add(summaryLabel, GridC.getc(0, 0).fillboth().east());
        return panel;
    }
}
