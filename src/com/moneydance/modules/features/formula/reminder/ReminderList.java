package com.moneydance.modules.features.formula.reminder;

import com.infinitekind.moneydance.model.Reminder;
import com.moneydance.apps.md.view.gui.MDAction;
import com.moneydance.apps.md.view.gui.MDImages;
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

        JComboBox boxSelect = new JComboBox(api.getReminders(false).toArray());
        reminderTable.getColumnModel().getColumn(0)
                .setCellEditor(new DefaultCellEditor(boxSelect));

        setLayout(new BorderLayout(UiUtil.DLG_HGAP, UiUtil.DLG_VGAP));
        setBorder(BorderFactory.createEmptyBorder(UiUtil.VGAP, UiUtil.HGAP,
                UiUtil.DLG_VGAP*2, UiUtil.HGAP));

        add(new JScrollPane(reminderTable), BorderLayout.CENTER);
        add(buildButtonPanel(api), BorderLayout.SOUTH);
    }

    public void addSelectionListener(final ReminderSelectionListener listener) {
        reminderTable.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) return;
            Reminder selectedRow = getSelectedRow();

            listener.selected(selectedRow);
        });
    }

    private JPanel buildButtonPanel(MDApi api) {
        MDAction addAction = MDAction.makeIconAction(api.getGUI(),
                api.getIcon(MDImages.PLUS),
                evt -> {
                    int index = tableModel.insert();
                    reminderTable.getSelectionModel().setSelectionInterval(index,index);
                });

        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.add(new JButton(addAction), GridC.getc(0, 0).north());
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
