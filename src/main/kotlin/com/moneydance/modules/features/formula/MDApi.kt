package com.moneydance.modules.features.formula

import com.moneydance.apps.md.controller.FeatureModuleContext
import com.moneydance.apps.md.view.gui.MoneydanceGUI
import com.moneydance.modules.features.formula.MDApi
import com.infinitekind.moneydance.model.Reminder
import java.util.stream.Collectors
import java.time.format.DateTimeFormatter
import com.infinitekind.moneydance.model.AccountBook
import com.moneydance.apps.md.controller.UserPreferences
import com.infinitekind.moneydance.model.CurrencyType
import com.infinitekind.util.CustomDateFormat
import java.time.LocalDate
import java.util.function.Supplier
import kotlin.jvm.JvmOverloads

class MDApi(context: FeatureModuleContext, val gui: MoneydanceGUI) {
    init {
        Companion.context = context
    }

    fun getReminders(isFormulaEnabled: Boolean): List<Reminder> {
        return book.reminders.allReminders
            .filter { reminder: Reminder -> isFormulaEnabled == reminder.getBooleanParameter(ENABLED_KEY, false) }
    }

    val shortDateFormatter: CustomDateFormat
        get() = gui.preferences.shortDateFormatter

    companion object {
        const val ENABLED_KEY = "formula"
        private var context: FeatureModuleContext? = null
        private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        @JvmStatic
        val book: AccountBook
            get() = context!!.currentAccountBook

        @JvmStatic
        fun enableReminder(reminder: Reminder) {
            reminder.setParameter(ENABLED_KEY, true)
            reminder.syncItem()
        }

        @JvmStatic
        fun disableReminder(reminder: Reminder) {
            reminder.removeParameter(ENABLED_KEY)
            reminder.syncItem()
        }

        val decimalChar: Char
            get() = UserPreferences.getInstance().decimalChar

        @JvmStatic
        fun formatCurrency(value: Long): String {
            val currencyType = book.currencies.baseType
            return currencyType.formatFancy(value, decimalChar)
        }

        @JvmStatic
        fun parseDate(date: Int): LocalDate {
            return LocalDate.parse(Integer.toString(date), formatter)
        }

        @JvmStatic
        @JvmOverloads
        fun log(msg: String, error: Throwable? = null) {
            println("moneydance-formula: $msg")
            if (error != null) {
                println(error.message)
                //            error.printStackTrace();
            }
        }
    }
}