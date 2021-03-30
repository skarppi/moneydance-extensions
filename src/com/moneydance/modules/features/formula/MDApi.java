package com.moneydance.modules.features.formula;

import com.infinitekind.moneydance.model.AccountBook;
import com.infinitekind.moneydance.model.CurrencyType;
import com.infinitekind.moneydance.model.Reminder;
import com.infinitekind.util.CustomDateFormat;
import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.apps.md.controller.UserPreferences;
import com.moneydance.apps.md.view.gui.MoneydanceGUI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MDApi {

    public static final String ENABLED_KEY = "formula";

    private static FeatureModuleContext context;

    private Supplier<MoneydanceGUI> gui;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public MDApi(FeatureModuleContext context, Supplier<MoneydanceGUI> gui) {
        this.gui = gui;
        MDApi.context = context;
    }

    public static AccountBook getBook() {
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

    public static char getDecimalChar() {
        return UserPreferences.getInstance().getDecimalChar();
    }

    public static String formatCurrency(long value) {
        CurrencyType currencyType = getBook().getCurrencies().getBaseType();
        return currencyType.formatFancy(value, getDecimalChar());
    }

    public MoneydanceGUI getGUI() {
        return gui.get();
    }

    public CustomDateFormat getShortDateFormatter() {
        return getGUI().getPreferences().getShortDateFormatter();
    }

    public static LocalDate parseDate(int date) {
        return LocalDate.parse(Integer.toString(date), formatter);
    }

    public static void log(String msg) {
        log(msg, null);
    }

    public static void log(String msg, Throwable error) {
        System.out.println("moneydance-formula: " + msg);
        if (error != null) {
            System.out.println(error.getMessage());
//            error.printStackTrace();
        }
    }
}
