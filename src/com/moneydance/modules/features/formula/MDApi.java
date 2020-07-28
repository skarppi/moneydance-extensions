package com.moneydance.modules.features.formula;

import com.infinitekind.moneydance.model.AccountBook;
import com.infinitekind.moneydance.model.CurrencyType;
import com.infinitekind.moneydance.model.Reminder;
import com.infinitekind.util.CustomDateFormat;
import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.apps.md.controller.UserPreferences;
import com.moneydance.apps.md.view.gui.MoneydanceGUI;

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
        reminder.setParameter(MDApi.ENABLED_KEY, true);
        reminder.syncItem();
    }

    public static void disableReminder(Reminder reminder) {
        reminder.removeParameter(MDApi.ENABLED_KEY);
        reminder.syncItem();
    }

    public CurrencyType getBaseCurrency() {
        return getBook().getCurrencies().getBaseType();
    }

    public String formatCurrency(long value) {
        char decimalChar = UserPreferences.getInstance().getDecimalChar();
        return getBaseCurrency().formatFancy(value, decimalChar);
    }

    public MoneydanceGUI getGUI() {
        return gui.get();
    }

    public CustomDateFormat getShortDateFormatter() {
        return getGUI().getPreferences().getShortDateFormatter();
    }
}
