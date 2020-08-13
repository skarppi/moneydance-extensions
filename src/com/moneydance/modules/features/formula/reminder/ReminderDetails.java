package com.moneydance.modules.features.formula.reminder;

import com.infinitekind.moneydance.model.*;
import com.moneydance.apps.md.view.gui.EditRemindersWindow;
import com.moneydance.apps.md.view.gui.MDAction;
import com.moneydance.awt.GridC;
import com.moneydance.modules.features.formula.MDApi;
import com.moneydance.util.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReminderDetails extends JPanel {

    private MDApi api;

    private Reminder reminder;

    private ParentTxn parentTxn;

    private SplitTxnTableModel tableModel;

    private JTable txTable;

    private JLabel summaryLabel;

    public ReminderDetails(MDApi mdApi) {
        api = mdApi;

        tableModel = new SplitTxnTableModel();

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

        tableModel.setTransactions(IntStream.range(0, parentTxn != null ? parentTxn.getSplitCount() : 0)
                .mapToObj(i -> new FormulaTxn(parentTxn.getSplit(i), tableModel))
                .collect(Collectors.toList()));

        summaryLabel.setText(summaryText());
    }

    public void storeSettings() {
        for(int i=0; i < parentTxn.getSplitCount(); i++) {
            FormulaTxn formulaTxn = tableModel.getTransactions().get(i);
            formulaTxn.syncSettings();
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
            FormulaTxn formulaTxn = tableModel.getTransactions().get(i);
            SplitTxn split = newTxn.getSplit(i);
            split.setAmount(formulaTxn.getAmount());
            split.setDescription(formulaTxn.getDescription());
        }

        txns.addNewTxn(newTxn);

        reminder.setAcknowledgedInt(date);
        reminder.syncItem();
    }

    private String summaryText() {
        long value = parentTxn != null ? parentTxn.getValue() : 0;
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
