package com.moneydance.modules.features.formula.reminder;

import com.infinitekind.moneydance.model.Reminder;
import com.moneydance.apps.md.view.gui.MDAction;
import com.moneydance.apps.md.view.gui.MDImages;
import com.moneydance.apps.md.view.gui.MoneydanceGUI;
import com.moneydance.awt.GridC;
import com.moneydance.modules.features.formula.MDApi;
import com.moneydance.util.UiUtil;

import javax.swing.*;
import java.awt.*;

public class ReminderList extends JPanel {

    private RemindersTableModel tableModel;
    private JTable reminderTable;

    public ReminderList(MDApi api) {
        tableModel = new RemindersTableModel(api.getReminders(true));
        reminderTable = new JTable(tableModel);
        getSelection().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JComboBox boxSelect = new JComboBox(api.getReminders(false).toArray());
        reminderTable.getColumnModel().getColumn(0)
                .setCellEditor(new DefaultCellEditor(boxSelect));

        setLayout(new BorderLayout(UiUtil.DLG_HGAP, UiUtil.DLG_VGAP));
        setBorder(BorderFactory.createEmptyBorder(UiUtil.VGAP, UiUtil.HGAP,
                UiUtil.DLG_VGAP*2, UiUtil.HGAP));

        add(new JScrollPane(reminderTable), BorderLayout.CENTER);
        add(buildButtonPanel(api), BorderLayout.SOUTH);
    }

    private ListSelectionModel getSelection() {
        return reminderTable.getSelectionModel();
    }

    public void addSelectionListener(final ReminderSelectionListener listener) {
        getSelection().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) return;
            Reminder selectedRow = getSelectedRow();

            listener.selected(selectedRow);
        });
    }

    private JPanel buildButtonPanel(MDApi api) {
        MoneydanceGUI gui = api.getGUI();
        MDAction addAction = MDAction.makeIconAction(gui,
                gui.getIcon(MDImages.PLUS),
                evt -> {
                    int index = tableModel.insert();
                    getSelection().setSelectionInterval(index,index);
                });

        MDAction removeAction = MDAction.makeIconAction(gui,
                gui.getIcon(MDImages.MINUS),
                evt -> {
                    int selectedRow = reminderTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        tableModel.remove(selectedRow);
                        getSelection().clearSelection();
                    }
                });

        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.add(new JButton(addAction), GridC.getc(0, 0).north());
        buttonPanel.add(new JButton(removeAction), GridC.getc(1, 0).fillboth());
        return buttonPanel;
    }

    public Reminder getSelectedRow() {
        int selectedRow = reminderTable.getSelectedRow();
        if (selectedRow >= 0) {
            return tableModel.getReminder(selectedRow);
        }
        return null;
    }

    public interface ReminderSelectionListener {
        void selected(Reminder selectedReminder);
    }
}
