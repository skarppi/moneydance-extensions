package com.moneydance.modules.features.formula;

import com.infinitekind.moneydance.model.AccountBook;
import com.infinitekind.moneydance.model.Reminder;
import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.apps.md.controller.Main;
import com.moneydance.apps.md.view.gui.MDImages;
import com.moneydance.apps.md.view.gui.MoneydanceGUI;

import javax.swing.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MDApi {

    public static final String ENABLED_KEY = "formula";

    private FeatureModuleContext context;

    private Supplier<MoneydanceGUI> gui;

    public MDApi(FeatureModuleContext context, Supplier<MoneydanceGUI> gui) {
        this.context = context;
        this.gui = gui;
    }

    public AccountBook getBook() {
        return context.getCurrentAccountBook();
    }

    public List<Reminder> getReminders(boolean isFormulaEnabled) {
        return getBook().getReminders().getAllReminders().stream()
                .filter(reminder -> isFormulaEnabled == reminder.getBooleanParameter(ENABLED_KEY, false))
                .collect(Collectors.toList());
    }

    public static void enableReminder(Reminder reminder) {
//            reminder.setDescription(reminder.getDescription() + "1");
        reminder.setParameter(MDApi.ENABLED_KEY, true);
        reminder.syncItem();
    }

    public MoneydanceGUI getGUI() {
        return gui.get();
    }

    public Icon getIcon(String path) {
        if (context instanceof com.moneydance.apps.md.controller.Main) {
            MoneydanceGUI ui = (MoneydanceGUI) ((Main) context).getUI();
            return ui.getIcon(path);
        } else {
            return MDImages.getMDIcon(path);
        }
    }
}
